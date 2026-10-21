package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.parser.exceptions.ParseException;

public class AddPersonDialogTest {
    private static final String LONG_VALIDATION_ERROR = "Emails should be of the format local-part@domain and "
            + "adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters. "
            + "The local-part may not start or end with any special characters.\n"
            + "2. The local-part is followed by an '@' and then a domain name.";

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
    public void fieldLabels_requiredFieldsMarked() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddPersonDialog dialog = new AddPersonDialog(owner);
            Platform.runLater(() -> checkRequiredFieldLabels(failure));

            dialog.showAndWait(command -> { });

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    @Test
    public void fieldFocus_borderWidthRemainsStable() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddPersonDialog dialog = new AddPersonDialog(owner);
            Platform.runLater(() -> checkFieldBorderWidths(failure));

            dialog.showAndWait(command -> { });

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    @Test
    public void validationError_longMessage_keepsDialogWidthAndContentVisible() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            AddPersonDialog dialog = new AddPersonDialog(owner);
            Platform.runLater(() -> submitInvalidFormAndCheckLayout(failure));

            dialog.showAndWait(command -> {
                throw new ParseException(LONG_VALIDATION_ERROR);
            });

            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        });
    }

    private void submitInvalidFormAndCheckLayout(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        double initialDialogWidth = dialogPane.getScene().getWindow().getWidth();
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
                assertEquals(LONG_VALIDATION_ERROR, errorLabel.getText());
                assertTrue(errorLabel.isVisible());
                assertTrue(dialogPane.getScene().getWindow().getWidth() <= initialDialogWidth,
                        "Validation errors should not widen the dialog");
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

    private void checkRequiredFieldLabels(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        try {
            GridPane form = (GridPane) dialogPane.lookup(".dialog-form");
            List<String> labels = form.getChildren().stream()
                    .filter(Label.class::isInstance)
                    .map(Label.class::cast)
                    .map(Label::getText)
                    .toList();
            assertEquals(List.of("Name *", "Phone *", "Email *", "Address *", "Tags (comma-separated)"), labels);
            Label requiredFieldNote = (Label) dialogPane.lookup(".required-field-note");
            assertEquals("* Required", requiredFieldNote.getText());
            assertTrue(requiredFieldNote.isVisible());
        } catch (Throwable throwable) {
            failure.set(throwable);
        } finally {
            dialogPane.getScene().getWindow().hide();
        }
    }

    private void checkFieldBorderWidths(AtomicReference<Throwable> failure) {
        DialogPane dialogPane = showingDialogPane();
        try {
            GridPane form = (GridPane) dialogPane.lookup(".dialog-form");
            List<TextField> fields = form.getChildren().stream()
                    .filter(TextField.class::isInstance)
                    .map(TextField.class::cast)
                    .toList();
            TextField field = fields.get(1);
            field.applyCss();
            double unfocusedBottomBorder = field.getBorder().getStrokes().getFirst().getWidths().getBottom();
            field.requestFocus();
            Platform.runLater(() -> checkFocusedBorderWidth(
                    failure, dialogPane, field, unfocusedBottomBorder));
        } catch (Throwable throwable) {
            failure.set(throwable);
            dialogPane.getScene().getWindow().hide();
        }
    }

    private void checkFocusedBorderWidth(AtomicReference<Throwable> failure, DialogPane dialogPane,
            TextField field, double unfocusedBottomBorder) {
        try {
            field.applyCss();
            assertTrue(field.isFocused());
            double focusedBottomBorder = field.getBorder().getStrokes().getFirst().getWidths().getBottom();
            assertEquals(unfocusedBottomBorder, focusedBottomBorder,
                    "Focusing a field should not change its border geometry");
        } catch (Throwable throwable) {
            failure.set(throwable);
        } finally {
            dialogPane.getScene().getWindow().hide();
        }
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
