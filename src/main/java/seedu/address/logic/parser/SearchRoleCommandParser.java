package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.FLAG_INCLUDE_ARCHIVE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/**
 * Parses a role phrase, optional {@code /event} scope, and {@code /include-archive} flag.
 * The phrase may contain multiple words and is search text rather than an assigned role.
 */
public class SearchRoleCommandParser implements Parser<SearchRoleCommand> {
    private static final Pattern ARGUMENT_FORMAT = Pattern.compile(
            "(?<keyword>.+?) /event (?<eventId>\\S+)");
    private static final Pattern COMMAND_PREFIX = Pattern.compile("(?:^|\\s)/\\S*");

    /**
     * Parses {@code ROLE [/event EVENTID] [/include-archive]}, preserving case and normalizing whitespace.
     * Rejects duplicate or unsupported options and misplaced event scopes or archive flags.
     *
     * @param args The arguments following the command word.
     * @return A command carrying the role phrase and optional validated event scope.
     * @throws ParseException if the query, optional event ID, or command structure is invalid.
     * @throws NullPointerException if {@code args} is null.
     */
    @Override
    public SearchRoleCommand parse(String args) throws ParseException {
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
            return new SearchRoleCommand(input, includeArchived);
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
        return new SearchRoleCommand(eventId, keyword, includeArchived);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE));
    }
}
