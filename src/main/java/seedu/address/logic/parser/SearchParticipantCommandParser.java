package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a name phrase followed by an optional /event scope and /include-archive flag.
 */
public class SearchParticipantCommandParser implements Parser<SearchParticipantCommand> {
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
        SearchArguments arguments = SearchArgumentsParser.parse(args,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchParticipantCommand.MESSAGE_USAGE));
        if (arguments.getEventId().isPresent()) {
            return new SearchParticipantCommand(arguments.getEventId().orElseThrow(), arguments.getKeyword(),
                    arguments.shouldIncludeArchived());
        }
        return new SearchParticipantCommand(arguments.getKeyword(), arguments.shouldIncludeArchived());
    }
}
