package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PARTICIPANT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.List;
import java.util.Set;

import seedu.address.logic.commands.EditParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;

/**
 * Parses arguments for {@link EditParticipantCommand}.
 */
public class EditParticipantCommandParser implements Parser<EditParticipantCommand> {

    @Override
    public EditParticipantCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithSeparatedPrefixes(
                args, PREFIX_ROLE, PREFIX_PARTICIPANT_TAG);
        String preamble = argMultimap.getPreamble();
        String[] arguments = preamble.isEmpty() ? new String[0] : preamble.split("\\s+");
        if (arguments.length != 2) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, EditParticipantCommand.MESSAGE_USAGE));
        }

        EventId eventId = ParserUtil.parseEventId(arguments[0]);
        PersonId personId = ParserUtil.parsePersonId(arguments[1]);
        List<String> roleValues = argMultimap.getAllValues(PREFIX_ROLE);
        List<String> tagValues = argMultimap.getAllValues(PREFIX_PARTICIPANT_TAG);
        if (roleValues.isEmpty() && tagValues.isEmpty()) {
            throw new ParseException(EditParticipantCommand.MESSAGE_NO_CHANGES);
        }
        Set<ParticipationRole> roles = roleValues.isEmpty() ? null
                : roleValues.equals(List.of("")) ? Set.of() : ParserUtil.parseParticipationRoles(roleValues);
        Set<Tag> tags = tagValues.isEmpty() ? null
                : tagValues.equals(List.of("")) ? Set.of() : ParserUtil.parseTags(tagValues);
        return new EditParticipantCommand(eventId, personId, roles, tags);
    }
}
