package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ArchiveEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a single stable event ID. */
public class ArchiveEventCommandParser implements Parser<ArchiveEventCommand> {
    @Override
    public ArchiveEventCommand parse(String args) throws ParseException {
        if (args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ArchiveEventCommand.MESSAGE_USAGE));
        }
        return new ArchiveEventCommand(ParserUtil.parseEventId(args.strip()));
    }
}
