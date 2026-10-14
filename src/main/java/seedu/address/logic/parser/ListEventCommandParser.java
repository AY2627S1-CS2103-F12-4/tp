package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ListEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the no-argument list-event command. */
public class ListEventCommandParser implements Parser<ListEventCommand> {
    @Override
    public ListEventCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListEventCommand.MESSAGE_USAGE));
        }
        return new ListEventCommand();
    }
}
