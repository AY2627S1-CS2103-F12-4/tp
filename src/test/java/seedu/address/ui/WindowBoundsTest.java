package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class WindowBoundsTest {
    @TempDir
    public Path temporaryFolder;

    private final Rectangle2D primary = new Rectangle2D(0, 0, 1280, 720);

    @Test
    public void fit_screenAbovePrimaryAndTopLeftOverflow_clampsToVisualOrigin() {
        Rectangle2D upper = new Rectangle2D(0, -900, 1440, 860);
        assertEquals(new Rectangle2D(0, -900, 740, 600),
                WindowBounds.fit(new Rectangle2D(-20, -920, 740, 600), List.of(primary, upper), primary));
    }

    @Test
    public void fit_widthAndHeightOverflow_areClampedIndependently() {
        assertEquals(new Rectangle2D(0, 40, 1280, 300),
                WindowBounds.fit(new Rectangle2D(0, 40, 2000, 300), List.of(primary), primary));
        assertEquals(new Rectangle2D(40, 0, 300, 720),
                WindowBounds.fit(new Rectangle2D(40, 0, 300, 2000), List.of(primary), primary));
    }

    @Test
    public void fit_smallerLaterOverlap_doesNotReplaceBestScreen() {
        Rectangle2D secondary = new Rectangle2D(1280, 0, 1920, 1080);
        assertEquals(new Rectangle2D(280, 0, 1000, 600),
                WindowBounds.fit(new Rectangle2D(400, -20, 1000, 600), List.of(primary, secondary), primary));
    }

    @Test
    public void fit_unpositionedStage_usesPrimaryOriginAndPreservesMinimums() throws Exception {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                stage.setWidth(Math.min(300, screen.getWidth()));
                stage.setHeight(Math.min(200, screen.getHeight()));
                stage.setMinWidth(100);
                stage.setMinHeight(80);
                WindowBounds.fit(stage);
                assertEquals(screen.getMinX(), stage.getX());
                assertEquals(screen.getMinY(), stage.getY());
                assertEquals(100, stage.getMinWidth());
                assertEquals(80, stage.getMinHeight());
                assertWithinScreen(stage);
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void mainWindow_show_fitsOversizedSavedSettingsWithoutChangingData() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            GuiSettings saved = new GuiSettings(10_000, 10_000, 1_000_000, 1_000_000);
            model.setGuiSettings(saved);
            StorageManager storage = new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("people.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    temporaryFolder.resolve("people.json"));
            try {
                window.fillInnerParts();
                window.show();
                assertWithinScreen(window.getPrimaryStage());
                assertEquals(saved, model.getGuiSettings());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void helpWindow_showAndReopen_fitsCurrentScreenAndKeepsCopyButtonAccessible() throws Exception {
        runOnFxThread(() -> {
            HelpWindow window = new HelpWindow();
            try {
                window.show();
                assertWithinScreen(window.getRoot());
                assertTrue(window.getRoot().getScene().lookup("#copyButton").isVisible());
                window.hide();
                window.getRoot().setWidth(10_000);
                window.getRoot().setHeight(10_000);
                window.show();
                assertWithinScreen(window.getRoot());
            } finally {
                window.hide();
            }
        });
    }

    private void assertWithinScreen(Stage stage) {
        assertTrue(Screen.getScreens().stream().map(Screen::getVisualBounds).anyMatch(screen ->
                stage.getX() >= screen.getMinX() - 1 && stage.getY() >= screen.getMinY() - 1
                && stage.getX() + stage.getWidth() <= screen.getMaxX() + 1
                && stage.getY() + stage.getHeight() <= screen.getMaxY() + 1));
    }

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
