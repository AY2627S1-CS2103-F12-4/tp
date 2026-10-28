package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PARTICIPANT_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Set;

import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;

/**
 * Parses arguments for {@link AddParticipantCommand}.
 */
public class AddParticipantCommandParser implements Parser<AddParticipantCommand> {

    @Override
    public AddParticipantCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithSeparatedPrefixes(
                args, PREFIX_ROLE, PREFIX_PARTICIPANT_TAG);
        String preamble = argMultimap.getPreamble();
        String[] arguments = preamble.isEmpty() ? new String[0] : preamble.split("\\s+");
        if (arguments.length != 2) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, AddParticipantCommand.MESSAGE_USAGE));
        }

        EventId eventId = ParserUtil.parseEventId(arguments[0]);
        PersonId personId = ParserUtil.parsePersonId(arguments[1]);
        Set<ParticipationRole> roles = ParserUtil.parseParticipationRoles(argMultimap.getAllValues(PREFIX_ROLE));
        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_PARTICIPANT_TAG));
        return new AddParticipantCommand(eventId, personId, roles, tags);
    }
}
