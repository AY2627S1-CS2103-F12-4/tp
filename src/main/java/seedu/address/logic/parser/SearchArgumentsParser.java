package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.FLAG_INCLUDE_ARCHIVE;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/**
 * Parses the shared argument structure of participant and role searches.
 */
final class SearchArgumentsParser {
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "(?<keyword>.+?) /event (?<eventId>\\S+)");
    private static final Pattern COMMAND_PREFIX = Pattern.compile("(?:^|\\s)/\\S*");

    private SearchArgumentsParser() {
    }

    /**
     * Parses a search phrase followed by an optional event scope and archive flag.
     * Preserves case and normalizes whitespace before validating the argument structure.
     *
     * @param args Arguments following the command word.
     * @param invalidFormatMessage Command-specific error message for malformed argument structure.
     * @return Normalized search arguments with a validated event ID when a scope is supplied.
     * @throws NullPointerException if either argument is null.
     * @throws ParseException if the argument structure or event ID is invalid.
     */
    static SearchArguments parse(String args, String invalidFormatMessage) throws ParseException {
        requireNonNull(args);
        requireNonNull(invalidFormatMessage);
        String input = StringUtil.normalizeWhitespace(args);
        String archiveFlagSuffix = " " + FLAG_INCLUDE_ARCHIVE;
        boolean shouldIncludeArchived = input.endsWith(archiveFlagSuffix);
        if (shouldIncludeArchived) {
            input = input.substring(0, input.length() - archiveFlagSuffix.length());
        }
        if (input.isEmpty()) {
            throw new ParseException(invalidFormatMessage);
        }
        if (!COMMAND_PREFIX.matcher(input).find()) {
            return new SearchArguments(input, Optional.empty(), shouldIncludeArchived);
        }
        Matcher matcher = ARGUMENT_FORMAT.matcher(input);
        if (!matcher.matches()) {
            throw new ParseException(invalidFormatMessage);
        }

        String keyword = matcher.group("keyword");
        if (COMMAND_PREFIX.matcher(keyword).find() || matcher.group("eventId").startsWith("/")) {
            throw new ParseException(invalidFormatMessage);
        }
        EventId eventId = ParserUtil.parseEventId(matcher.group("eventId"));
        return new SearchArguments(keyword, Optional.of(eventId), shouldIncludeArchived);
    }
}
