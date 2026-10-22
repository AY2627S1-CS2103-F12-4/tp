package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.FLAG_INCLUDE_ARCHIVE;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.ListEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses list-event with an optional standalone /include-archive flag. */
public class ListEventCommandParser implements Parser<ListEventCommand> {
    @Override
    public ListEventCommand parse(String args) throws ParseException {
        String input = StringUtil.normalizeWhitespace(args);
        if (input.isEmpty()) {
            return new ListEventCommand();
        }
        if (input.equals(FLAG_INCLUDE_ARCHIVE)) {
            return new ListEventCommand(true);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListEventCommand.MESSAGE_USAGE));
    }
}
