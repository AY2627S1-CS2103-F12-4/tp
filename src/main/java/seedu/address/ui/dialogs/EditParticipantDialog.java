package seedu.address.ui.dialogs;

import static seedu.address.logic.parser.CliSyntax.PREFIX_PARTICIPANT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.EditParticipantCommand;
import seedu.address.model.mapper.Participation;

/** Collects replacement roles and tags for an existing participation. */
public final class EditParticipantDialog extends FormDialog {
    private final String eventId;
    private final String personId;
    private final TextField roles;
    private final TextField tags;

    /** Creates a pre-filled edit-participation dialog. */
    public EditParticipantDialog(Stage owner, Participation participation) {
        super(owner, "Edit participation",
                "Edit " + participation.getPerson().getName().fullName + " in "
                        + participation.getEvent().getName() + ".", "Edit");
        eventId = participation.getEvent().getId().toString();
        personId = participation.getPerson().getId().toString();
        roles = addTextField(0, "Roles (comma-separated)", "Facilitator, Guest Speaker", false);
        tags = addTextField(1, "Tags (comma-separated)", "VIP, Alumni", false);

        roles.setText(participation.getRoles().stream().map(Object::toString).sorted()
                .collect(Collectors.joining(", ")));
        tags.setText(participation.getTags().stream().map(Object::toString).sorted()
                .collect(Collectors.joining(", ")));
    }

    /** Displays the dialog and submits replacement participation details. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(roles, commandExecutor, () ->
                buildCommand(eventId, personId, roles.getText(), tags.getText()));
    }

    static String buildCommand(String eventId, String personId, String roles, String tags) {
        return EditParticipantCommand.COMMAND_WORD + " " + eventId + " " + personId
                + formatArguments(roles, PREFIX_ROLE.toString())
                + formatArguments(tags, PREFIX_PARTICIPANT_TAG.toString());
    }

    private static String formatArguments(String values, String prefix) {
        String arguments = Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> " " + prefix + " " + value)
                .collect(Collectors.joining());
        return arguments.isEmpty() ? " " + prefix : arguments;
    }
}
