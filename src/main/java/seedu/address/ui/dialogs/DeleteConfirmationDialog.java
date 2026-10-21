package seedu.address.ui.dialogs;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/** Displays an app-themed confirmation before deleting an entry. */
public final class DeleteConfirmationDialog {
    private static final ButtonType DELETE_BUTTON = new ButtonType("Delete", ButtonData.OK_DONE);

    private final Dialog<ButtonType> dialog = new Dialog<>();

    /** Creates a confirmation dialog with the supplied title and target description. */
    public DeleteConfirmationDialog(Stage owner, String title, String header) {
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("This action cannot be undone.");
        dialog.getDialogPane().setId("deleteConfirmationDialog");
        dialog.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        dialog.getDialogPane().getButtonTypes().addAll(DELETE_BUTTON, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> buttonType);

        Button deleteButton = (Button) dialog.getDialogPane().lookupButton(DELETE_BUTTON);
        deleteButton.getStyleClass().add("destructive-button");
    }

    /** Shows the dialog and returns whether deletion was confirmed. */
    public boolean showAndWait() {
        return dialog.showAndWait().filter(DELETE_BUTTON::equals).isPresent();
    }
}
