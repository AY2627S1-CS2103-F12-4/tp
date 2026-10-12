package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an event index and a single /name search phrase.
 */
public class SearchParticipantCommandParser implements Parser<SearchParticipantCommand> {
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "\\s*(?<index>\\S+)\\s+/name\\s+(?<keyword>.*?)\\s*", Pattern.DOTALL);
    private static final Pattern NAME_PREFIX = Pattern.compile("(?:^|\\s)/name(?:\\s|$)");

    @Override
    public SearchParticipantCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher matcher = ARGUMENT_FORMAT.matcher(args);
        if (!matcher.matches()) {
            throw invalidFormat();
        }

        String keyword = matcher.group("keyword").strip();
        if (keyword.isBlank() || NAME_PREFIX.matcher(keyword).find()) {
            throw invalidFormat();
        }
        Index eventIndex = ParserUtil.parseIndex(matcher.group("index"));
        return new SearchParticipantCommand(eventIndex, keyword);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                SearchParticipantCommand.MESSAGE_USAGE));
    }
}
