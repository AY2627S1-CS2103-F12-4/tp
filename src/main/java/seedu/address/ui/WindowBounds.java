package seedu.address.ui;

import java.util.List;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Keeps windows inside a currently available screen's usable area. */
public final class WindowBounds {
    private WindowBounds() {
    }

    /** Uses the screen with the largest overlap, falling back to the primary screen. */
    static Rectangle2D fit(Rectangle2D requested, List<Rectangle2D> screens, Rectangle2D primary) {
        Rectangle2D target = primary;
        double largestOverlap = 0;
        for (Rectangle2D screen : screens) {
            double overlap = Math.max(0, Math.min(requested.getMaxX(), screen.getMaxX())
                    - Math.max(requested.getMinX(), screen.getMinX()))
                    * Math.max(0, Math.min(requested.getMaxY(), screen.getMaxY())
                    - Math.max(requested.getMinY(), screen.getMinY()));
            if (overlap > largestOverlap) {
                target = screen;
                largestOverlap = overlap;
            }
        }
        double width = Math.min(requested.getWidth(), target.getWidth());
        double height = Math.min(requested.getHeight(), target.getHeight());
        double x = largestOverlap == 0 ? target.getMinX() + (target.getWidth() - width) / 2
                : Math.max(target.getMinX(), Math.min(requested.getMinX(), target.getMaxX() - width));
        double y = largestOverlap == 0 ? target.getMinY() + (target.getHeight() - height) / 2
                : Math.max(target.getMinY(), Math.min(requested.getMinY(), target.getMaxY() - height));
        return new Rectangle2D(x, y, width, height);
    }

    /** Fits a sized window, relaxing stage minimum dimensions only when the screen requires it. */
    public static void fit(Window window) {
        Rectangle2D primary = Screen.getPrimary().getVisualBounds();
        Rectangle2D bounds = fit(new Rectangle2D(
                Double.isFinite(window.getX()) ? window.getX() : primary.getMinX(),
                Double.isFinite(window.getY()) ? window.getY() : primary.getMinY(),
                window.getWidth(), window.getHeight()),
                Screen.getScreens().stream().map(Screen::getVisualBounds).toList(), primary);
        if (window instanceof Stage stage) {
            stage.setMinWidth(Math.min(stage.getMinWidth(), bounds.getWidth()));
            stage.setMinHeight(Math.min(stage.getMinHeight(), bounds.getHeight()));
        }
        window.setWidth(bounds.getWidth());
        window.setHeight(bounds.getHeight());
        window.setX(bounds.getMinX());
        window.setY(bounds.getMinY());
    }
}
