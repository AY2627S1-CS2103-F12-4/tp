package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.FLAG_INCLUDE_ARCHIVE;

import java.util.regex.Pattern;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Parses an event-name keyword followed by an optional /include-archive flag.
 */
public class FindEventCommandParser implements Parser<FindEventCommand> {
    private static final Pattern COMMAND_PREFIX = Pattern.compile("(?:^|\\s)/\\S*");

    @Override
    public FindEventCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String keyword = args.trim();
        String[] tokens = keyword.split("\\s+");
        boolean shouldIncludeArchived = tokens[tokens.length - 1].equals(FLAG_INCLUDE_ARCHIVE);
        if (shouldIncludeArchived) {
            keyword = keyword.substring(0, keyword.length() - FLAG_INCLUDE_ARCHIVE.length()).trim();
        }
        if (keyword.isEmpty() || COMMAND_PREFIX.matcher(keyword).find()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                            FindEventCommand.MESSAGE_USAGE));
        }

        return new FindEventCommand(
                new EventNameContainsKeywordPredicate(keyword), shouldIncludeArchived);
    }
}
