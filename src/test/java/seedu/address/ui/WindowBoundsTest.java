package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowBoundsTest {
    private final Rectangle2D primary = new Rectangle2D(0, 0, 1280, 720);

    @Test
    public void fit_validWindow_preservesBounds() {
        Rectangle2D requested = new Rectangle2D(100, 50, 740, 600);
        assertEquals(requested, WindowBounds.fit(requested, List.of(primary), primary));
    }

    @Test
    public void fit_disconnectedMonitor_centersOnPrimary() {
        assertEquals(new Rectangle2D(270, 60, 740, 600),
                WindowBounds.fit(new Rectangle2D(2000, 100, 740, 600), List.of(primary), primary));
    }

    @Test
    public void fit_smallerThanMinimumScreen_shrinksToUsableBounds() {
        Rectangle2D small = new Rectangle2D(0, 30, 480, 400);
        assertEquals(small, WindowBounds.fit(new Rectangle2D(0, 0, 740, 600), List.of(small), small));
    }

    @Test
    public void fit_negativeMonitorAndPartialOverlap_keepsWindowOnThatMonitor() {
        Rectangle2D secondary = new Rectangle2D(-1280, 20, 1280, 700);
        assertEquals(new Rectangle2D(-740, 120, 740, 600),
                WindowBounds.fit(new Rectangle2D(-600, 300, 740, 600), List.of(primary, secondary), primary));
    }

    @Test
    public void fit_spanningScreens_usesLargestOverlap() {
        Rectangle2D secondary = new Rectangle2D(1280, 0, 1920, 1080);
        assertEquals(new Rectangle2D(1280, 100, 1000, 600),
                WindowBounds.fit(new Rectangle2D(1100, 100, 1000, 600), List.of(primary, secondary), primary));
    }

    @Test
    public void fit_stageOutsideScreens_recoversInsidePrimaryVisualBounds() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            stage.setMinWidth(screen.getWidth() + 500);
            stage.setMinHeight(screen.getHeight() + 500);
            stage.setWidth(screen.getWidth() + 500);
            stage.setHeight(screen.getHeight() + 500);
            stage.setX(1_000_000);
            stage.setY(1_000_000);
            WindowBounds.fit(stage);
            assertEquals(screen.getMinX(), stage.getX());
            assertEquals(screen.getMinY(), stage.getY());
            assertEquals(screen.getWidth(), stage.getWidth());
            assertEquals(screen.getHeight(), stage.getHeight());
            assertTrue(stage.getMinWidth() <= screen.getWidth());
            assertTrue(stage.getMinHeight() <= screen.getHeight());
            stage.close();
        });
    }

    @Test
    public void helpWindow_isResizableAndWrapsMessage() throws Exception {
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow();
            assertTrue(window.getRoot().isResizable());
            Label label = (Label) window.getRoot().getScene().lookup("#helpMessage");
            assertTrue(label.isWrapText());
            window.hide();
        });
    }
}
