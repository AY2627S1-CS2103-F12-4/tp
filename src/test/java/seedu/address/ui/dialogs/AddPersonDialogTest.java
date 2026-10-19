package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.parser.exceptions.ParseException;

public class AddPersonDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30 t/friend t/volunteer",
                AddPersonDialog.buildCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30",
                        "friend, volunteer"));
    }

    @Test
    public void buildCommand_blankTags_omitsTagPrefixes() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30",
                AddPersonDialog.buildCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30", " "));
    }

    @Test
    public void validationError_longMessage_keepsDescriptionAndButtonBarVisible() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddPersonDialog dialog = new AddPersonDialog(owner);
            Platform.runLater(() -> submitInvalidFormAndCheckLayout(failure));

            dialog.showAndWait(command -> {
                throw new ParseException("Names should only contain alphanumeric characters and spaces, "
                        + "and should not be blank");
            });

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    private void submitInvalidFormAndCheckLayout(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        Button addButton = (Button) dialogPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType.getText().equals("Add"))
                .map(dialogPane::lookupButton)
                .findFirst()
                .orElseThrow();
        addButton.fire();

        Platform.runLater(() -> {
            try {
                Bounds buttonBounds = addButton.localToScene(addButton.getBoundsInLocal());
                Node form = dialogPane.lookup(".dialog-form");
                Bounds formBounds = form.localToScene(form.getBoundsInLocal());
                Label errorLabel = (Label) dialogPane.lookup(".validation-error-label");
                Bounds errorBounds = errorLabel.localToScene(errorLabel.getBoundsInLocal());
                assertEquals("Enter the person's details.", dialogPane.getHeaderText());
                assertEquals("Names should only contain alphanumeric characters and spaces, "
                        + "and should not be blank", errorLabel.getText());
                assertTrue(errorLabel.isVisible());
                assertTrue(formBounds.getMaxY() <= errorBounds.getMinY(),
                        "Validation errors should appear below the form fields");
                assertTrue(errorBounds.getMaxY() <= buttonBounds.getMinY(),
                        "Validation errors should not overlap the button bar");
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                dialogPane.getScene().getWindow().hide();
            }
        });
    }

    private DialogPane showingDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .map(scene -> scene.getRoot())
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow();
    }
}
