package seedu.address.ui;

import java.util.Arrays;
import java.util.stream.Collectors;

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

/** Displays forms that translate person and event details into existing CLI commands. */
public final class AddEntryDialog {
    private static final ButtonType ADD_BUTTON = new ButtonType("Add", ButtonData.OK_DONE);

    private AddEntryDialog() {}

    /** Displays the form for adding a person. */
    public static void showAddPersonDialog(Stage owner, CommandExecutor commandExecutor) {
        Dialog<Void> dialog = createDialog(owner, "Add person", "Enter the person's details.");
        TextField name = createField("Full name");
        TextField phone = createField("Phone number");
        TextField email = createField("Email address");
        TextField address = createField("Home address");
        TextField tags = createField("friend, volunteer");

        GridPane form = createForm();
        addField(form, 0, "Name", name);
        addField(form, 1, "Phone", phone);
        addField(form, 2, "Email", email);
        addField(form, 3, "Address", address);
        addField(form, 4, "Tags (comma-separated)", tags);
        dialog.getDialogPane().setContent(form);

        configureSubmission(dialog, () -> commandExecutor.execute(
                buildAddPersonCommand(name.getText(), phone.getText(), email.getText(), address.getText(),
                        tags.getText())));
        dialog.setOnShown(event -> name.requestFocus());
        dialog.showAndWait();
    }

    /** Displays the form for adding an event. */
    public static void showAddEventDialog(Stage owner, CommandExecutor commandExecutor) {
        Dialog<Void> dialog = createDialog(owner, "Add event", "Enter the event's details.");
        TextField name = createField("Event name");
        TextField capacity = createField("Maximum participants");
        TextField description = createField("Event description");

        GridPane form = createForm();
        addField(form, 0, "Name", name);
        addField(form, 1, "Capacity", capacity);
        addField(form, 2, "Description", description);
        dialog.getDialogPane().setContent(form);

        configureSubmission(dialog, () -> commandExecutor.execute(
                buildAddEventCommand(name.getText(), capacity.getText(), description.getText())));
        dialog.setOnShown(event -> name.requestFocus());
        dialog.showAndWait();
    }

    static String buildAddPersonCommand(String name, String phone, String email, String address, String tags) {
        String tagArguments = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> " t/" + tag)
                .collect(Collectors.joining());
        return String.format("add n/%s p/%s e/%s a/%s%s", name.trim(), phone.trim(), email.trim(),
                address.trim(), tagArguments);
    }

    static String buildAddEventCommand(String name, String capacity, String description) {
        return String.format("add-event %s /cap %s /desc %s", name.trim(), capacity.trim(), description.trim());
    }

    private static Dialog<Void> createDialog(Stage owner, String title, String header) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().setId("addEntryDialog");
        dialog.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        dialog.getDialogPane().getButtonTypes().addAll(ADD_BUTTON, ButtonType.CANCEL);
        return dialog;
    }

    private static GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        return form;
    }

    private static TextField createField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefColumnCount(28);
        return field;
    }

    private static void addField(GridPane form, int row, String labelText, TextField field) {
        form.add(new Label(labelText), 0, row);
        form.add(field, 1, row);
    }

    private static void configureSubmission(Dialog<Void> dialog, CommandSubmission submission) {
        Button addButton = (Button) dialog.getDialogPane().lookupButton(ADD_BUTTON);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                submission.submit();
            } catch (CommandException | ParseException exception) {
                dialog.setHeaderText(exception.getMessage());
                event.consume();
            }
        });
    }

    /** Executes a command assembled by an add-entry form. */
    @FunctionalInterface
    public interface CommandExecutor {
        void execute(String command) throws CommandException, ParseException;
    }

    @FunctionalInterface
    private interface CommandSubmission {
        void submit() throws CommandException, ParseException;
    }
}
