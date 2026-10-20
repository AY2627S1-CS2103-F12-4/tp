package seedu.address.ui.dialogs;

import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Collects the parameters required to add an event. */
public final class AddEventDialog extends FormDialog {
    private final TextField name;
    private final TextField capacity;
    private final TextField description;

    /** Creates an add-event dialog owned by the given stage. */
    public AddEventDialog(Stage owner) {
        super(owner, "Add event", "Enter the event's details.");
        name = addTextField(0, "Name", "Event name", true);
        capacity = addTextField(1, "Capacity", "Maximum participants", true);
        description = addTextField(2, "Description", "Event description", false);
    }

    /** Displays the dialog and submits valid event details through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(name, commandExecutor, () ->
                buildCommand(name.getText(), capacity.getText(), description.getText()));
    }

    static String buildCommand(String name, String capacity, String description) {
        return String.format("add-event %s /cap %s /desc %s", name.trim(), capacity.trim(), description.trim());
    }
}
