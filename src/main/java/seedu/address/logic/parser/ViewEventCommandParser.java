package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a single stable event ID for view-event. */
public class ViewEventCommandParser implements Parser<ViewEventCommand> {
    @Override
    public ViewEventCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.isEmpty() || trimmed.split("\\s+").length != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewEventCommand.MESSAGE_USAGE));
        }
        return new ViewEventCommand(ParserUtil.parseEventId(trimmed));
    }
}
