package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a role phrase, optional {@code /event} scope, and {@code /include-archive} flag.
 * The phrase may contain multiple words and is search text rather than an assigned role.
 */
public class SearchRoleCommandParser implements Parser<SearchRoleCommand> {
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
        SearchArguments arguments = SearchArgumentsParser.parse(args,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE));
        if (arguments.getEventId().isPresent()) {
            return new SearchRoleCommand(arguments.getEventId().orElseThrow(), arguments.getKeyword(),
                    arguments.shouldIncludeArchived());
        }
        return new SearchRoleCommand(arguments.getKeyword(), arguments.shouldIncludeArchived());
    }
}
