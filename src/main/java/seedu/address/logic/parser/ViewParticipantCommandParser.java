
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;
import seedu.address.model.person.PersonId;

/**
 * Parses arguments for ViewParticipantCommand.
 */
public class ViewParticipantCommandParser
        implements Parser<ViewParticipantCommand> {

    @Override
    public ViewParticipantCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        String[] arguments = trimmedArgs.isEmpty()
                ? new String[0] : trimmedArgs.split("\\s+");

        if (arguments.length != 2) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT,
                    ViewParticipantCommand.MESSAGE_USAGE));
        }

        EventId eventId = ParserUtil.parseEventId(arguments[0]);
        PersonId personId = ParserUtil.parsePersonId(arguments[1]);

        return new ViewParticipantCommand(eventId, personId);
    }
}
