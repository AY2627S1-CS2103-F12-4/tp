package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;

/**
 * Searches participation roles across nonarchived events, optionally scoped to one event ID.
 * Filters events and participant results without modifying stored registrations.
 */
public class SearchRoleCommand extends Command {
    public static final String COMMAND_WORD = "search-role";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Searches participant roles in nonarchived events using a case-insensitive substring.\n"
            + "Parameters: ROLE [/event EVENTID]\n"
            + "Example: " + COMMAND_WORD + " Best Man /event E1";
    public static final String MESSAGE_SUCCESS =
            "%1$d %2$s across %3$d %4$s with roles matching \"%5$s\".";
    public static final String MESSAGE_NO_MATCHES =
            "No registrations with roles matching \"%s\" in nonarchived events.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";

    private final Optional<EventId> eventId;
    private final String keyword;

    /**
     * Creates a search for the specified event and complete role phrase.
     * Normalizes query whitespace while preserving the case used in feedback.
     *
     * @param eventId ID of the event to search.
     * @param keyword Nonblank search text, which need not satisfy assigned-role constraints.
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if {@code keyword} is blank.
     */
    public SearchRoleCommand(EventId eventId, String keyword) {
        this(Optional.of(eventId), keyword);
    }

    /** Creates a role search across all nonarchived events. */
    public SearchRoleCommand(String keyword) {
        this(Optional.empty(), keyword);
    }

    private SearchRoleCommand(Optional<EventId> eventId, String keyword) {
        String normalizedKeyword = StringUtil.normalizeWhitespace(keyword);
        checkArgument(!normalizedKeyword.isEmpty(), "Role search keyword must not be blank.");
        this.eventId = eventId;
        this.keyword = normalizedKeyword;
    }

    /**
     * Updates role-search results and event filters independently of existing display filters.
     * Counts a registration once even when several roles match; each event membership counts separately.
     *
     * @throws CommandException if the event does not exist or cannot be retrieved from the mapper.
     * @throws NullPointerException if {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (eventId.isPresent()) {
            EventId scope = eventId.orElseThrow();
            try {
                model.updateParticipantRoleSearch(scope, keyword);
            } catch (EventNotFoundException e) {
                throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, scope), e);
            }
        } else {
            model.updateParticipantRoleSearch(keyword);
        }

        int registrationCount = model.getParticipantSearchResults().size();
        if (registrationCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, keyword), false, false, true);
        }

        int eventCount = model.getFilteredEventList().size();
        return new CommandResult(String.format(MESSAGE_SUCCESS, registrationCount,
                registrationCount == 1 ? "registration" : "registrations", eventCount,
                eventCount == 1 ? "event" : "events", keyword), false, false, true);
    }

    /**
     * Returns whether both commands use the same event scope and normalized feedback query.
     */
    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchRoleCommand otherCommand
                && eventId.equals(otherCommand.eventId)
                && keyword.equals(otherCommand.keyword);
    }

    /**
     * Returns the hash code of the optional event scope and whitespace-normalized query.
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventId, keyword);
    }
}
