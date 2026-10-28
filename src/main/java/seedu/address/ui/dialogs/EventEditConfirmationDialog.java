package seedu.address.ui.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/** Confirms event edits containing text that resembles an unsupported field prefix. */
public final class EventEditConfirmationDialog {
    private static final ButtonType CONFIRM_BUTTON = new ButtonType("Confirm edit", ButtonData.OK_DONE);
    private static final ButtonType CANCEL_BUTTON = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

    private final Dialog<ButtonType> dialog = new Dialog<>();

    /** Creates a confirmation showing the proposed edit before any data is changed. */
    public EventEditConfirmationDialog(Stage owner, String preview) {
        dialog.initOwner(owner);
        dialog.setTitle("Confirm event edit");
        dialog.setHeaderText("Possible field prefix typo");
        dialog.setContentText(preview);
        dialog.getDialogPane().setId("eventEditConfirmationDialog");
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        dialog.getDialogPane().getButtonTypes().addAll(CONFIRM_BUTTON, CANCEL_BUTTON);
        Button confirm = (Button) dialog.getDialogPane().lookupButton(CONFIRM_BUTTON);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(CANCEL_BUTTON);
        confirm.setDefaultButton(false);
        cancel.setDefaultButton(true);
        dialog.setOnShown(event -> cancel.requestFocus());
    }

    /** Returns true only when the user explicitly confirms the edit. */
    public boolean showAndWait() {
        return dialog.showAndWait().filter(CONFIRM_BUTTON::equals).isPresent();
    }
}
