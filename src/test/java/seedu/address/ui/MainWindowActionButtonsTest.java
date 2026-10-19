package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowActionButtonsTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void actionButtons_loaded_haveVisibleLabelsAndActions() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                Button addPersonButton = (Button) window.getPrimaryStage().getScene().lookup("#addPersonButton");
                Button addEventButton = (Button) window.getPrimaryStage().getScene().lookup("#addEventButton");

                assertNotNull(addPersonButton);
                assertNotNull(addEventButton);
                assertEquals("+ Person", addPersonButton.getText());
                assertEquals("+ Event", addEventButton.getText());
                assertNotNull(addPersonButton.getOnAction());
                assertNotNull(addEventButton.getOnAction());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow() {
        ModelManager model = new ModelManager();
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("people.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("people.json"));
        window.fillInnerParts();
        return window;
    }
}
