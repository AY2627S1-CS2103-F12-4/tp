package seedu.address.ui.dialogs;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddParticipantCommand;

/** Collects the person ID required to add a participant to an event. */
public final class AddParticipantDialog extends FormDialog {
    private final String eventId;
    private final TextField personId;

    /** Creates an add-participant dialog for the supplied event. */
    public AddParticipantDialog(Stage owner, String eventId, String eventName) {
        super(owner, "Add participant", "Add participant to " + eventName + ".");
        this.eventId = eventId;
        personId = addTextField(0, "Person ID", "e.g. P1", true);
    }

    /** Displays the dialog and submits the person ID through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(personId, commandExecutor, () -> buildCommand(eventId, personId.getText()));
    }

    static String buildCommand(String eventId, String personId) {
        return String.format("%s %s %s", AddParticipantCommand.COMMAND_WORD, eventId, personId.trim());
    }
}
