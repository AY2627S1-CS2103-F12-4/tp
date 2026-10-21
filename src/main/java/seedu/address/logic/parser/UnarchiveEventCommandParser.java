package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UnarchiveEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a single stable event ID. */
public class UnarchiveEventCommandParser implements Parser<UnarchiveEventCommand> {
    @Override
    public UnarchiveEventCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, UnarchiveEventCommand.MESSAGE_USAGE));
        }
        return new UnarchiveEventCommand(ParserUtil.parseEventId(args.strip()));
    }
}
