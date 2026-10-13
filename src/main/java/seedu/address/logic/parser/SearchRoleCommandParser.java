package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

/**
 * Parses an event ID and a single {@code /role} search phrase into a {@code SearchRoleCommand}.
 * The phrase may contain multiple words and is search text rather than an assigned role.
 */
public class SearchRoleCommandParser implements Parser<SearchRoleCommand> {
    /**
     * Parses arguments in the form {@code EVENT_ID /role KEYWORD}.
     * Collapses whitespace before tokenization so tabs also separate the event ID and prefix.
     * Preserves query case and rejects duplicate role prefixes rather than taking the last value.
     *
     * @param args The arguments following the command word.
     * @return A command searching the specified event for the complete role phrase.
     * @throws ParseException if the event ID is invalid, the event ID or query is missing,
     *     the query is blank, or the role prefix is repeated.
     * @throws NullPointerException if {@code args} is null.
     */
    @Override
    public SearchRoleCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args.replaceAll("\\s+", " "), PREFIX_ROLE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_ROLE);

        String eventIdText = arguments.getPreamble();
        String keyword = arguments.getValue(PREFIX_ROLE).orElse("");
        if (eventIdText.isBlank() || keyword.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE));
        }

        EventId eventId = ParserUtil.parseEventId(eventIdText);
        return new SearchRoleCommand(eventId, keyword);
    }
}
