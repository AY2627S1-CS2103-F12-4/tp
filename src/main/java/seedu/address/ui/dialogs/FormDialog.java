package seedu.address.ui.dialogs;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.ui.WindowBounds;

/** Provides the shared layout and submission behavior for entry dialogs. */
abstract class FormDialog {
    private static final double HEADER_PREF_WIDTH = 480;
    private static final double MAX_HEADER_HEIGHT = 200;

    private final Dialog<Void> dialog = new Dialog<>();
    private final GridPane form = new GridPane();
    private final Label requiredFieldNote = new Label("* Required");
    private final Label validationErrorLabel = new Label();
    private final ButtonType submitButton;
    private final Map<TextField, String> requiredFields = new LinkedHashMap<>();

    FormDialog(Stage owner, String title, String header) {
        this(owner, title, header, "Add");
    }

    FormDialog(Stage owner, String title, String header, String submitButtonText) {
        submitButton = new ButtonType(submitButtonText, ButtonData.OK_DONE);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        Label headerLabel = new Label(header);
        headerLabel.setMinWidth(0);
        headerLabel.setPrefWidth(HEADER_PREF_WIDTH);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setWrapText(true);
        ScrollPane headerScroll = new ScrollPane(headerLabel);
        headerScroll.setFitToWidth(true);
        headerScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        headerScroll.setPrefViewportWidth(HEADER_PREF_WIDTH);
        headerScroll.prefViewportHeightProperty().bind(Bindings.createDoubleBinding(() -> Math.min(
                MAX_HEADER_HEIGHT, headerLabel.prefHeight(HEADER_PREF_WIDTH)),
                headerLabel.fontProperty(), headerLabel.skinProperty()));
        headerScroll.setMaxHeight(MAX_HEADER_HEIGHT);
        VBox headerPane = new VBox(headerScroll);
        headerPane.getStyleClass().add("header-panel");
        dialog.getDialogPane().setHeader(headerPane);
        dialog.getDialogPane().setId("addEntryDialog");
        dialog.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.getStyleClass().add("dialog-form");

        requiredFieldNote.getStyleClass().add("required-field-note");
        requiredFieldNote.setManaged(false);
        requiredFieldNote.setVisible(false);

        validationErrorLabel.getStyleClass().add("validation-error-label");
        validationErrorLabel.setMaxWidth(Double.MAX_VALUE);
        validationErrorLabel.prefWidthProperty().bind(form.widthProperty());
        validationErrorLabel.setWrapText(true);
        validationErrorLabel.setManaged(false);
        validationErrorLabel.setVisible(false);

        dialog.getDialogPane().setContent(new VBox(8, form, requiredFieldNote, validationErrorLabel));
    }

    TextField addTextField(int row, String labelText, String promptText, boolean isRequired) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefColumnCount(28);
        form.add(new Label(labelText + (isRequired ? " *" : "")), 0, row);
        form.add(field, 1, row);
        if (isRequired) {
            requiredFields.put(field, labelText);
            requiredFieldNote.setManaged(true);
            requiredFieldNote.setVisible(true);
        }
        return field;
    }

    void showAndWait(TextField initialFocus, CommandExecutor commandExecutor, CommandSupplier commandSupplier) {
        Button submitButtonNode = (Button) dialog.getDialogPane().lookupButton(submitButton);
        submitButtonNode.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                for (Map.Entry<TextField, String> requiredField : requiredFields.entrySet()) {
                    if (requiredField.getKey().getText().isBlank()) {
                        requiredField.getKey().requestFocus();
                        throw new ParseException(requiredField.getValue() + " is required.");
                    }
                }
                commandExecutor.execute(commandSupplier.get());
            } catch (CommandException | ParseException exception) {
                validationErrorLabel.setText(exception.getMessage());
                validationErrorLabel.setManaged(true);
                validationErrorLabel.setVisible(true);
                Platform.runLater(() -> dialog.getDialogPane().getScene().getWindow().sizeToScene());
                event.consume();
            }
        });
        Window window = dialog.getDialogPane().getScene().getWindow();
        InvalidationListener fitOnResize = observable -> {
            if (window.isShowing()) {
                WindowBounds.fit(window);
            }
        };
        window.widthProperty().addListener(fitOnResize);
        window.heightProperty().addListener(fitOnResize);
        // The window is positioned after Dialog's shown event when using showAndWait.
        window.setOnShown(event -> {
            WindowBounds.fit(window);
            initialFocus.requestFocus();
        });
        dialog.showAndWait();
    }

    /** Supplies a command after validating the form's field values. */
    @FunctionalInterface
    interface CommandSupplier {
        String get() throws ParseException;
    }
}
