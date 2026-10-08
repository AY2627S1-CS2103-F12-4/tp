package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Parses the arguments of find-event into a FindEventCommand.
 */
public class FindEventCommandParser implements Parser<FindEventCommand> {

    @Override
    public FindEventCommand parse(String args) throws ParseException {
        String keyword = args.trim();
        if (keyword.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            FindEventCommand.MESSAGE_USAGE));
        }

        return new FindEventCommand(
                new EventNameContainsKeywordPredicate(keyword));
    }
}
