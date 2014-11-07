package me.lokesh.lamp.ui;

import com.google.common.eventbus.EventBus;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import me.lokesh.lamp.Shared;
import me.lokesh.lamp.events.SettingsUpdatedEvent;
import me.lokesh.lamp.service.Config;
import me.lokesh.lamp.service.LAMPService;
import me.lokesh.lamp.ui.multiscreen.ControlledScreen;
import me.lokesh.lamp.ui.multiscreen.ScreensPane;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ResourceBundle;

public class SignupController implements Initializable, ControlledScreen {
    private final static Logger logger = LoggerFactory.getLogger(SignupController.class);
    private final static EventBus eventBus = Shared.getEventBus();

    public TextField deviceNameTextField;
    public Button startButton;
    public Button folderChooser;

    final DirectoryChooser directoryChooser = new DirectoryChooser();

    private GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
    private StringProperty musicFolderProperty;
    private StringProperty deviceNameProperty;

    private ScreensPane screensPane;

    public void submit() {
        deviceNameProperty.setValue(deviceNameTextField.getText().trim());

        logger.debug("Saving registration info: devicename={}, musicFolderpath={}",
                deviceNameProperty.getValue(), musicFolderProperty.getValue());

        Config.save(deviceNameProperty.getValue(), musicFolderProperty.getValue());
        screensPane.setScreen("main");

        if(!LAMPService.isStarted()) {
            LAMPService.start();
        }

        eventBus.post(new SettingsUpdatedEvent());
    }

    @Override
    public void setScreenParent(ScreensPane screenPage) {
        screensPane = screenPage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        eventBus.register(this);

        folderChooser.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN).size(12));
        startButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHEVRON_CIRCLE_RIGHT).size(16));

        musicFolderProperty = Config.getMusicFolderPathString();
        deviceNameProperty = Config.getDeviceName();

        folderChooser.textProperty().bindBidirectional(musicFolderProperty, new Format() {
            @Override
            public StringBuffer format(Object obj, @NotNull StringBuffer toAppendTo, @NotNull FieldPosition pos) {
                if(obj.toString().isEmpty()) {
                    return new StringBuffer("Select Music folder ...");
                } else {
                    return new StringBuffer(obj.toString());
                }
            }

            @Override
            public Object parseObject(String source, @NotNull ParsePosition pos) {
                return source;
            }
        });

        if(Config.isRegistered()) {
            deviceNameTextField.setText(Config.getDeviceName().getValue());
        }
//        deviceNameTextField.textProperty().bindBidirectional(deviceNameProperty);

        BooleanBinding nonEmptyBinding = new BooleanBinding() {
            {
                super.bind(deviceNameTextField.textProperty(), musicFolderProperty);
            }

            @Override
            protected boolean computeValue() {
                return (deviceNameTextField.getText().trim().isEmpty()
                        || (musicFolderProperty.getValue().isEmpty())
                );
            }
        };

        startButton.disableProperty().bind(nonEmptyBinding);

        StringBinding startButtonTextBinding = new StringBinding() {
            {
                super.bind(Config.getRegisteredProperty());
            }

            @Override
            protected String computeValue() {
                if (Config.isRegistered()) {
                    return "Update";
                } else {
                    return "Start";
                }
            }
        };

        startButton.textProperty().bind(startButtonTextBinding);
    }

    public void selectMusicFolder() {
        directoryChooser.setTitle("Select Music Folder");

        if(musicFolderProperty.getValue().isEmpty()) {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            directoryChooser.setInitialDirectory(new File(musicFolderProperty.getValue()));
        }

        File chosenFolder = directoryChooser.showDialog(screensPane.getScene().getWindow());

        if(chosenFolder != null) {
            musicFolderProperty.setValue(chosenFolder.getAbsolutePath());
            logger.debug("music folder path={}", chosenFolder.getAbsolutePath());
        }

    }
}
