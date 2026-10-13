
package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListParticipantsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/**
 * Parses arguments for ListParticipantsCommand.
 */
public class ListParticipantsCommandParser
        implements Parser<ListParticipantsCommand> {

    @Override
    public ListParticipantsCommand parse(String args)
            throws ParseException {

        String[] arguments = args.trim().split("\\s+");

        if (args.isBlank() || arguments.length != 1) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT,
                    ListParticipantsCommand.MESSAGE_USAGE));
        }

        EventId eventId = ParserUtil.parseEventId(arguments[0]);

        return new ListParticipantsCommand(eventId);
    }
}
