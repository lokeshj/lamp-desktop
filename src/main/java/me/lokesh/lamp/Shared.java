package me.lokesh.lamp;

import com.google.common.eventbus.EventBus;
import javafx.scene.image.Image;

/**
 * Created by lokesh.
 */
public class Shared {
    private static final EventBus eventBus = new EventBus();

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static Image getLogoImage(double width, double height) {
        return new Image("/images/icon.png", width, height, true, true);
    }

    public static Image getLogoImage() {
        return new Image("/images/icon.png");
    }
}
