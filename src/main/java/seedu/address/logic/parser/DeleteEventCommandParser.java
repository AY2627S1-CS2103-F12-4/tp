package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses one stable event ID for del-event. */
public class DeleteEventCommandParser implements Parser<DeleteEventCommand> {
    @Override
    public DeleteEventCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty() || trimmed.split("\\s+").length != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
        }
        return new DeleteEventCommand(ParserUtil.parseEventId(trimmed));
    }
}
