package seedu.address.ui.dialogs;

import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/** Provides the shared layout and submission behavior for add-entry dialogs. */
abstract class FormDialog {
    private static final ButtonType ADD_BUTTON = new ButtonType("Add", ButtonData.OK_DONE);
    private static final String VALIDATION_ERROR_STYLE_CLASS = "validation-error";

    private final Dialog<Void> dialog = new Dialog<>();
    private final GridPane form = new GridPane();

    FormDialog(Stage owner, String title, String header) {
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setId("addEntryDialog");
        dialog.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        dialog.getDialogPane().getButtonTypes().addAll(ADD_BUTTON, ButtonType.CANCEL);

        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.getStyleClass().add("dialog-form");
        dialog.getDialogPane().setContent(form);
    }

    TextField addTextField(int row, String labelText, String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefColumnCount(28);
        form.add(new Label(labelText), 0, row);
        form.add(field, 1, row);
        return field;
    }

    void showAndWait(TextField initialFocus, CommandExecutor commandExecutor, Supplier<String> commandSupplier) {
        Button addButton = (Button) dialog.getDialogPane().lookupButton(ADD_BUTTON);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                commandExecutor.execute(commandSupplier.get());
            } catch (CommandException | ParseException exception) {
                dialog.setHeaderText(exception.getMessage());
                if (!dialog.getDialogPane().getStyleClass().contains(VALIDATION_ERROR_STYLE_CLASS)) {
                    dialog.getDialogPane().getStyleClass().add(VALIDATION_ERROR_STYLE_CLASS);
                }
                Platform.runLater(() -> dialog.getDialogPane().getScene().getWindow().sizeToScene());
                event.consume();
            }
        });
        dialog.setOnShown(event -> initialFocus.requestFocus());
        dialog.showAndWait();
    }
}
