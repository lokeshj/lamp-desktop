package me.lokesh.lamp.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.*;
import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.LAMPService;
import me.lokesh.lamp.service.models.Peer;
import me.lokesh.lamp.service.models.PeerStatus;
import me.lokesh.lamp.service.player.Track;
import me.lokesh.lamp.service.search.SearchAgent;
import me.lokesh.lamp.service.utils.SystemProperties;
import me.lokesh.lamp.ui.multiscreen.ControlledScreen;
import me.lokesh.lamp.ui.multiscreen.ScreensPane;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainController implements Initializable, ControlledScreen {
    private final EventBus eventBus = Shared.getEventBus();
    private ScreensPane screensPane;

    public Label logo;
    public Button settingsButton;
    public TextField searchTextfield;
    public Button searchButton;
    public Label deviceNameLabel;
    public VBox mainContentArea;
    public Label trackLabel;
    public Circle connectionStatus;
    public ListView<String> searchResultListview;
    public StackPane mainContentPane;
    public ListView<Peer> peerListView;
    public ListView<String> libraryListView;
    public HBox mainToolbarBox;
    public Button stopButton;
    public Label searchDescription;
    public VBox searchResultCtr;
    public VBox libraryCtr;
    public Label libraryDescription;

    private ToggleButton peersButton;
    private ToggleButton libraryButton;
    private ToggleButton searchResultsButton;

    private List<Track> searchResultTrackList;
    private List<Track> libraryTrackList;
    private ObservableList<Peer> peerList;
    private Peer lastSelectedPeer;

    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final String onlineFill = "#02cd50";
    private final String offlineFill = "#e1380e";

    private ExecutorService searchExecutor = Executors.newCachedThreadPool();
    private ExecutorCompletionService<List<Track>> searchCompletionService;
    private ExecutorService libraryLoaderExecutor = Executors.newCachedThreadPool();
    private ExecutorCompletionService<List<Track>> libraryLoadCompletionService;

    private final Peer localhost = new Peer(Config.getDeviceName().getValue(),
            SystemProperties.getOs(), SystemProperties.getIPAddress());

    @Override
    public void setScreenParent(ScreensPane screenPage) {
        screensPane = screenPage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventBus.register(this);

        logo.setGraphic(new ImageView(Shared.getLogoImage(32.0, 32.0)));
        settingsButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.COG).size(16));
        searchButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.SEARCH).size(12));

        deviceNameLabel.textProperty().bind(Config.getDeviceName());

        peersButton = new ToggleButton("Peers (0)");
        libraryButton = new ToggleButton("Shared Library");
        searchResultsButton = new ToggleButton("Search Results");

        peersButton.setSelected(true);
        searchResultsButton.setVisible(false);

        SegmentedButton segmentedButton_dark =
                new SegmentedButton(peersButton, libraryButton, searchResultsButton);
        segmentedButton_dark.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        mainToolbarBox.getChildren().add(segmentedButton_dark);

        peersButton.setOnMouseClicked(event -> {
            if (peersButton.isSelected()) {
                peerListView.toFront();
            } else {
                peersButton.setSelected(true);
            }
        });

        libraryButton.setOnMouseClicked((event) -> {
            if (libraryButton.isSelected()) {
                libraryCtr.toFront();
            } else  {
                libraryButton.setSelected(true);
            }
        });

        searchResultsButton.setOnMouseClicked((event) -> {
            if (searchResultsButton.isSelected()) {
                searchResultCtr.toFront();
            } else  {
                searchResultsButton.setSelected(true);
            }
        });

        searchResultListview.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                //Use ListView's getSelected Item
                int itemIndex = searchResultListview.getSelectionModel().getSelectedIndex();
                if(itemIndex >= 0) {
                    showPeerChoiceDialogAndPlay(searchResultTrackList.get(itemIndex));
                }
            }
        });

        peerList = FXCollections.observableArrayList();
        peerListView.setItems(peerList);
        peerListView.setCellFactory(param -> new PeerListViewCell());

        libraryListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                //Use ListView's getSelected Item
                int itemIndex = libraryListView.getSelectionModel().getSelectedIndex();
                if (itemIndex >= 0) {
                    showPeerChoiceDialogAndPlay(libraryTrackList.get(itemIndex));
                }
            }
        });

        peerListView.setPlaceholder(new Label("No Peers are online"));
        libraryListView.setPlaceholder(new Label("Loading ..."));

        refreshLibrary(null);
    }

    private void showPeerChoiceDialogAndPlay(Track selectedItem) {
        //if no one is online than start playback on localhost without showing
        //the pointless choice dialog
        if (peerList.size() == 0) {
            lastSelectedPeer = localhost;
            eventBus.post(new StartRemotePlaybackEvent(localhost.getIpAddress(), selectedItem));
            return;
        }

        List<Peer> choices = new LinkedList<>();
        choices.add(localhost);
        choices.addAll(peerList.stream().collect(Collectors.toList()));

        ChoiceDialog<Peer> dialog;
        if(lastSelectedPeer == null) {
            dialog = new ChoiceDialog<>(localhost, choices);
        } else {
            dialog = new ChoiceDialog<>(lastSelectedPeer, choices);
        }

        dialog.setHeaderText("Play it on?");
        dialog.setContentText("");
        dialog.setTitle("Select device");
        dialog.setResizable(true);

        Optional<Peer> result = dialog.showAndWait();
        if (result.isPresent()) {
            lastSelectedPeer = result.get();
            eventBus.post(new StartRemotePlaybackEvent(lastSelectedPeer.getIpAddress(), selectedItem));
        }
    }

    private void refreshLibrary(Peer loadLibraryOf) {
        libraryLoadCompletionService = new ExecutorCompletionService<>(libraryLoaderExecutor);
        libraryTrackList = new LinkedList<>();

        int userCount = peerList.size() + 1;
        String descriptionStaticText = " Songs from " + userCount + " users";
        String emptyLabel = "No MP3 files found";

        searchAndPopulate(loadLibraryOf, libraryLoadCompletionService, libraryLoaderExecutor,
                "", libraryTrackList, libraryDescription, descriptionStaticText,
                libraryListView, emptyLabel, null, null);
    }

    @FXML
    public void search() {
        String query = searchTextfield.getText().trim().replaceAll("\"", "");
        if (query.isEmpty()) {
            return;
        }

        searchResultListview.setPlaceholder(new Label("Searching ..."));
        searchCompletionService = new ExecutorCompletionService<>(searchExecutor);
        searchResultTrackList = new LinkedList<>();

        int userCount = peerList.size() + 1;
        String staticText = " Results for \"" + query + "\" from " + userCount + " users";
        String emptyLabel = "No Results found";

        searchAndPopulate(null, searchCompletionService, searchExecutor, query,
                searchResultTrackList, searchDescription, staticText, searchResultListview,
                emptyLabel, searchResultsButton, searchResultCtr);
    }

    private void searchAndPopulate(final Peer target,
                                   final ExecutorCompletionService<List<Track>> completionService,
                                   final ExecutorService executor,
                                   String query, List<Track> trackList,
                                   Label descriptionLabel, String descriptionStaticText,
                                   ListView<String> listView, String emptyLabel,
                                   ToggleButton button, VBox container)
    {
        if(target == null) {
            completionService.submit(() -> SearchAgent.remote(localhost.getIpAddress(), query));
            for (Peer peer : peerList) {
                completionService.submit(() -> SearchAgent.remote(peer.getIpAddress(), query));
            }
        } else {
            completionService.submit(() -> SearchAgent.remote(target.getIpAddress(), query));
        }


        Platform.runLater(() -> {
            if(button != null && container != null) {
                button.setSelected(true);
                button.setVisible(true);
                container.toFront();
            }

            SimpleIntegerProperty resultCount = new SimpleIntegerProperty(0);
            descriptionLabel.textProperty().bind(Bindings.concat(resultCount, descriptionStaticText));

            ObservableList<String> nameList = FXCollections.observableArrayList();
            listView.setItems(nameList);

            //execute this in another thread so that
            //ui remains responsive
            executor.execute(() -> {
                int numTarget = 1;
                if (target != null) {
                    numTarget = peerList.size() + 1;
                }

                for (int i = 0; i < numTarget; ++i) {
                    try {
                        List<Track> results = completionService.take().get();
                        for (Track result : results) {
                            Platform.runLater(() -> {
                                resultCount.setValue(resultCount.getValue() + 1);
                                nameList.add(result.getName());
                                trackList.add(result);
                            });
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                if (nameList.size() == 0) {
                    Platform.runLater(() -> listView.setPlaceholder(new Label(emptyLabel)));
                }
            });

        });
    }

    public void showSettings() {
        screensPane.setScreen("signup");
    }

    @Subscribe
    public void onPlaybackStart(PlaybackStartedEvent event) {
        Platform.runLater(() -> {
            try {
                stopButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.STOP).size(12));
                stopButton.setPrefHeight(32.0);
                stopButton.setPrefWidth(60.0);
                stopButton.setMinWidth(60.0);
                stopButton.setVisible(true);
                System.out.println("playing "+event.getTrack());
                String track = URLDecoder.decode(event.getTrack().getName(), "UTF-8");
                trackLabel.setText(new File(track).getName());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    @Subscribe
    public void onPlaybackEnd(PlaybackStoppedEvent event) {
        Platform.runLater(() -> {
            trackLabel.setText("Not Playing Anything ...");
            stopButton.setPrefHeight(0);
            stopButton.setPrefWidth(0);
            stopButton.setMinWidth(0);
            stopButton.setVisible(false);
            stopButton.setGraphic(null);
        });
    }

    @Subscribe
    public void onNetworkSuccessEvent(NetworkSuccessEvent event) {
        Platform.runLater(() -> connectionStatus.setFill(Color.valueOf(onlineFill)));
    }

    @Subscribe
    public void onNetworkErrorEvent(NetworkErrorEvent event) {
        Platform.runLater(() -> connectionStatus.setFill(Color.valueOf(offlineFill)));
    }

    @Subscribe
    public void onPeerOnlineEvent(PeerOnlineEvent event) {
        System.out.println("peer online" + event.getPeer());
        Platform.runLater(() -> {
            peerList.add(event.getPeer());
            peersButton.setText("Peers (" + peerList.size() + ")");
            refreshLibrary(null);
        });
    }

    @Subscribe
    public void onPeerOfflineEvent(PeerOfflineEvent event) {
        System.out.println("peer offline" + event.getPeer());
        Platform.runLater(() -> {
            peerList.remove(event.getPeer());
            peersButton.setText("Peers (" + peerList.size() + ")");
            refreshLibrary(null);
        });
    }

    @Subscribe
    public void onPeerUpdateEvent(PeerUpdateEvent event) {
        System.out.println("peer updated old=" + event.getOldPeer() + ", new= " + event.getNewPeer());
        Platform.runLater(() -> {
            peerList.remove(event.getOldPeer());
            peerList.add(event.getNewPeer());
        });
    }

    @Subscribe
    public void onSettingsUpdatedEvent(SettingsUpdatedEvent event) {
        refreshLibrary(null);
    }

    @FXML
    public void stopPlayback() {
        eventBus.post(new StopPlaybackEvent());
    }

    class PeerListViewCell extends ListCell<Peer>{
        @Override
        protected void updateItem(Peer item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                String ip = item.getIpAddress();
                ObservableMap<String, PeerStatus> statusMap = LAMPService.getPeerManager().getPeerStatusMap();
                PeerStatus peerStatus = statusMap.get(ip);

                String labelText = item.getName();
                if (peerStatus.isPlaying()) {
                    labelText += " ( " + peerStatus.getTrack() + " )";
                    setGraphic(fontAwesome.create(FontAwesome.Glyph.PLAY_CIRCLE).size(16.0));
                } else {
                    setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).size(16.0));
                }

                setText(labelText);
            }
        }
    }
}
