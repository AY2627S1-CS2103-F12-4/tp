package seedu.address.ui.dialogs;

import static seedu.address.logic.parser.CliSyntax.PREFIX_PARTICIPANT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;

/** Collects the details required to add a participant to an event. */
public final class AddParticipantDialog extends FormDialog {
    private final String eventId;
    private final TextField personId;
    private final TextField roles;
    private final TextField tags;

    /** Creates an add-participant dialog for the supplied event. */
    public AddParticipantDialog(Stage owner, String eventId, String eventName) {
        super(owner, "Add participant", "Add participant to " + eventName + ".");
        this.eventId = eventId;
        personId = addTextField(0, "Person ID", "e.g. P1", true);
        roles = addTextField(1, "Roles (comma-separated)", "Facilitator, Guest Speaker", false);
        tags = addTextField(2, "Tags (comma-separated)", "VIP, Alumni", false);
    }

    /** Displays the dialog and submits the participant details through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(personId, commandExecutor, () ->
                buildCommand(eventId, personId.getText(), roles.getText(), tags.getText()));
    }

    static String buildCommand(String eventId, String personId, String roles, String tags) throws ParseException {
        String validatedPersonId = ParserUtil.parsePersonId(personId).toString();
        DialogInputValidator.validateParticipation(roles, tags);
        String roleArguments = formatArguments(roles, PREFIX_ROLE.toString());
        String tagArguments = formatArguments(tags, PREFIX_PARTICIPANT_TAG.toString());
        return String.format("%s %s %s%s%s", AddParticipantCommand.COMMAND_WORD, eventId, validatedPersonId,
                roleArguments, tagArguments);
    }

    /** Returns repeated CLI arguments for the comma-separated values. */
    private static String formatArguments(String values, String prefix) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> " " + prefix + " " + value)
                .collect(Collectors.joining());
    }
}
