package me.lokesh.lamp.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import me.lokesh.lamp.service.player.Track;
import me.lokesh.lamp.service.search.AsyncRecursiveDirectoryStream;
import me.lokesh.lamp.service.search.SearchAgent;
import me.lokesh.lamp.service.utils.SystemProperties;
import me.lokesh.lamp.ui.multiscreen.ControlledScreen;
import me.lokesh.lamp.ui.multiscreen.ScreensPane;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.*;
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
    public Hyperlink deviceNameLabel;
    public VBox mainContenArea;
    public Label trackLabel;
    public Circle connectionStatus;
    public ListView<String> searchResultListview;
    public StackPane mainContentPane;
    public ListView<Peer> peerListview;
    public ListView<String> libraryListView;
    public HBox mainToolbarBox;

    private ToggleButton networkBtn;
    private ToggleButton libraryButton;
    private ToggleButton searchResultsButton;

    private List<Track> searchResultUrlList;
    private List<Track> libraryList;
    private ObservableList<Peer> peerList;
    private Peer lastSelectedPeer;

    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final String onlineFill = "#02cd50";
    private final String offlineFill = "#e1380e";

    private ExecutorService searchExecutor = Executors.newCachedThreadPool();
    ExecutorCompletionService<List<Track>> completionService;

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
        deviceNameLabel.setOnMouseClicked(event -> {
            libraryButton.setSelected(true);
            libraryListView.toFront();
        });

        networkBtn = new ToggleButton("Network");
        libraryButton = new ToggleButton("Library");
        searchResultsButton = new ToggleButton("Search Results");

        networkBtn.setSelected(true);
        searchResultsButton.setVisible(false);

        SegmentedButton segmentedButton_dark =
                new SegmentedButton(networkBtn, libraryButton, searchResultsButton);
        segmentedButton_dark.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        mainToolbarBox.getChildren().add(segmentedButton_dark);

        networkBtn.setOnMouseClicked(event -> {
            if (networkBtn.isSelected()) {
                peerListview.toFront();
            } else {
                networkBtn.setSelected(true);
            }
        });

        libraryButton.setOnMouseClicked((event) -> {
            if (libraryButton.isSelected()) {
                libraryListView.toFront();
            } else  {
                libraryButton.setSelected(true);
            }
        });

        searchResultsButton.setOnMouseClicked((event) -> {
            if (searchResultsButton.isSelected()) {
                searchResultListview.toFront();
            } else  {
                searchResultsButton.setSelected(true);
            }
        });

        searchResultListview.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                //Use ListView's getSelected Item
                int itemIndex = searchResultListview.getSelectionModel().getSelectedIndex();
                showPeerChoiceDialogAndPlay(searchResultUrlList.get(itemIndex));
            }
        });

        peerList = FXCollections.observableArrayList();
        peerListview.setItems(peerList);
        peerListview.setCellFactory(param -> new PeerListViewCell());

        loadLibrary();
        libraryListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                //Use ListView's getSelected Item
                int itemIndex = libraryListView.getSelectionModel().getSelectedIndex();
                showPeerChoiceDialogAndPlay(libraryList.get(itemIndex));
            }
        });

        peerListview.setPlaceholder(new Label("No Peers are online"));
        libraryListView.setPlaceholder(new Label("No MP3 files found"));
    }

    private void loadLibrary() {
        Platform.runLater(() -> {
            ObservableList<String> list = FXCollections.observableArrayList();
            libraryListView.setItems(list);

            List<Track> results = SearchAgent.remote("localhost", "");
            libraryList = new ArrayList<>();

            for (Track result : results) {
                list.add(result.getName());
                libraryList.add(result);
            }
        });
    }

    private void showPeerChoiceDialogAndPlay(Track selectedItem) {
        List<Peer> choices = new LinkedList<>();
        Peer local = new Peer();
        local.setIpAddress(SystemProperties.getIPAddress());
        local.setName(Config.getDeviceName().getValue());
        choices.add(local);
        choices.addAll(peerList.stream().collect(Collectors.toList()));


        ChoiceDialog<Peer> dialog;
        if(lastSelectedPeer == null) {
            dialog = new ChoiceDialog<>(local, choices);
        } else {
            dialog = new ChoiceDialog<>(lastSelectedPeer, choices);
        }

        dialog.setHeaderText("Play it on?");
        dialog.setContentText("");
        dialog.setTitle("Select device");

        Optional<Peer> result = dialog.showAndWait();
        if (result.isPresent()) {
            lastSelectedPeer = result.get();
            eventBus.post(new StartRemotePlaybackEvent(lastSelectedPeer.getIpAddress(), selectedItem));
        }
    }

    @FXML
    public void search() {
        String query = searchTextfield.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        searchResultListview.setPlaceholder(new Label("Searching ..."));

        completionService = new ExecutorCompletionService<>(searchExecutor);
        completionService.submit(() -> SearchAgent.remote("localhost", query));
        for (Peer peer : peerList) {
            completionService.submit(() -> SearchAgent.remote(peer.getIpAddress(), query));
        }

        searchResultUrlList = new LinkedList<>();

        Platform.runLater(() -> {
            searchResultsButton.setSelected(true);
            searchResultsButton.setVisible(true);
            searchResultListview.toFront();

            ObservableList<String> nameList = FXCollections.observableArrayList();
            searchResultListview.setItems(nameList);

            for (int i = 0; i < peerList.size() + 1; ++i) {
                try {
                    List<Track> results = completionService.take().get();
                    for (Track result : results) {
                        nameList.add(result.getName());
                        searchResultUrlList.add(result);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (nameList.size() == 0) {
                searchResultListview.setPlaceholder(new Label("No Results found"));
            }
        });
    }

    public void showSettings() {
        screensPane.setScreen("signup");
    }

    @Subscribe
    public void onPlaybackStart(PlaybackStartedEvent event) {
        Platform.runLater(() -> {
            try {
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
        Platform.runLater(() -> trackLabel.setText("Not Playing Anything ..."));
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
        Platform.runLater(() -> peerList.add(event.getPeer()));
    }

    @Subscribe
    public void onPeerOfflineEvent(PeerOfflineEvent event) {
        System.out.println("peer offline" + event.getPeer());
        Platform.runLater(() -> peerList.remove(event.getPeer()));
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
        loadLibrary();
    }

    class PeerListViewCell extends ListCell<Peer>{
        @Override
        protected void updateItem(Peer item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                setText(item.getName());
            }
        }
    }
}
