package seedu.address.ui.dialogs;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.EditEventCommand;
import seedu.address.model.event.Event;

/** Collects replacement details for an existing event. */
public final class EditEventDialog extends FormDialog {
    private final String eventId;
    private final TextField name;
    private final TextField capacity;
    private final TextField description;

    /** Creates a pre-filled edit-event dialog for the supplied event. */
    public EditEventDialog(Stage owner, Event event) {
        super(owner, "Edit event", "Edit " + event.getName() + ".", "Edit");
        eventId = event.getId();
        name = addTextField(0, "Name", "Event name", true);
        capacity = addTextField(1, "Capacity", "Maximum participants", true);
        description = addTextField(2, "Description", "Event description", false);

        name.setText(event.getName());
        capacity.setText(Integer.toString(event.getCapacity()));
        description.setText(event.getDescription());
    }

    /** Displays the dialog and submits the replacement details through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(name, commandExecutor, () ->
                buildCommand(eventId, name.getText(), capacity.getText(), description.getText()));
    }

    static String buildCommand(String eventId, String name, String capacity, String description) {
        return String.format("%s %s %s /cap %s /desc %s", EditEventCommand.COMMAND_WORD, eventId,
                name.trim(), capacity.trim(), description.trim());
    }
}
