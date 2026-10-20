package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DeleteConfirmationDialogTest {

    @Test
    public void showAndWait_deleteButton_returnsTrueAndUsesAppTheme() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(
                    owner, "Delete person", "Delete Alex Yeoh?");
            Platform.runLater(() -> confirmDeletion(failure));

            assertTrue(dialog.showAndWait());

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    private void confirmDeletion(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        try {
            assertEquals("deleteConfirmationDialog", dialogPane.getId());
            assertEquals("Delete Alex Yeoh?", dialogPane.getHeaderText());
            assertTrue(dialogPane.getStylesheets().stream().anyMatch(path -> path.endsWith("DarkTheme.css")));
            Button deleteButton = (Button) dialogPane.getButtonTypes().stream()
                    .filter(buttonType -> buttonType.getText().equals("Delete"))
                    .map(dialogPane::lookupButton)
                    .findFirst()
                    .orElseThrow();
            assertTrue(deleteButton.getStyleClass().contains("destructive-button"));
            deleteButton.fire();
        } catch (Throwable throwable) {
            failure.set(throwable);
            dialogPane.getScene().getWindow().hide();
        }
    }

    private DialogPane showingDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .map(Scene::getRoot)
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow();
    }
}
