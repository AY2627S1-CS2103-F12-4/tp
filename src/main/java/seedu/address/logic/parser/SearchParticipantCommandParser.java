package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.FLAG_INCLUDE_ARCHIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/**
 * Parses a name phrase followed by an optional /event scope and /include-archive flag.
 */
public class SearchParticipantCommandParser implements Parser<SearchParticipantCommand> {
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "(?<keyword>.+?) /event (?<eventId>\\S+)");
    private static final Pattern COMMAND_PREFIX = Pattern.compile("(?:^|\\s)/\\S*");

    /**
     * Parses {@code NAME [/event EVENTID] [/include-archive]}, preserving case and normalizing whitespace.
     * Rejects duplicate or unsupported options and misplaced event scopes or archive flags.
     *
     * @param args Arguments following the command word.
     * @return A command carrying the name phrase and optional validated event scope.
     * @throws NullPointerException if the arguments are null.
     * @throws ParseException if the event ID, name phrase, or command structure is invalid.
     */
    @Override
    public SearchParticipantCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String input = StringUtil.normalizeWhitespace(args);
        String archiveFlagSuffix = " " + FLAG_INCLUDE_ARCHIVE;
        boolean includeArchived = input.endsWith(archiveFlagSuffix);
        if (includeArchived) {
            input = input.substring(0, input.length() - archiveFlagSuffix.length());
        }
        if (input.isEmpty()) {
            throw invalidFormat();
        }
        if (!COMMAND_PREFIX.matcher(input).find()) {
            return new SearchParticipantCommand(input, includeArchived);
        }
        Matcher matcher = ARGUMENT_FORMAT.matcher(input);
        if (!matcher.matches()) {
            throw invalidFormat();
        }

        String keyword = matcher.group("keyword");
        if (COMMAND_PREFIX.matcher(keyword).find() || matcher.group("eventId").startsWith("/")) {
            throw invalidFormat();
        }
        EventId eventId = ParserUtil.parseEventId(matcher.group("eventId"));
        return new SearchParticipantCommand(eventId, keyword, includeArchived);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                SearchParticipantCommand.MESSAGE_USAGE));
    }
}
