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
 * Searches participation roles with optional event scope and archive inclusion.
 * Filters events and participant results without modifying stored registrations.
 */
public class SearchRoleCommand extends Command {
    public static final String COMMAND_WORD = "search-role";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Searches participant roles using a case-insensitive substring.\n"
            + "Archived events are excluded unless /include-archive is supplied.\n"
            + "Parameters: ROLE [/event EVENTID] [/include-archive]\n"
            + "Example: " + COMMAND_WORD + " Best Man /event E1 /include-archive";
    public static final String MESSAGE_SUCCESS =
            "%1$d %2$s across %3$d %4$s with roles matching \"%5$s\".";
    public static final String MESSAGE_NO_MATCHES =
            "No registrations with roles matching \"%s\" in nonarchived events.";
    public static final String MESSAGE_NO_MATCHES_INCLUDING_ARCHIVED =
            "No registrations with roles matching \"%s\" (including archived events).";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";

    private final Optional<EventId> eventId;
    private final String keyword;
    private final boolean includeArchived;

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
        this(eventId, keyword, false);
    }

    /** Creates a scoped role search with the requested archive visibility. */
    public SearchRoleCommand(EventId eventId, String keyword, boolean includeArchived) {
        this(Optional.of(eventId), keyword, includeArchived);
    }

    /** Creates a role search across all nonarchived events. */
    public SearchRoleCommand(String keyword) {
        this(keyword, false);
    }

    /** Creates a global role search with the requested archive visibility. */
    public SearchRoleCommand(String keyword, boolean includeArchived) {
        this(Optional.empty(), keyword, includeArchived);
    }

    private SearchRoleCommand(Optional<EventId> eventId, String keyword, boolean includeArchived) {
        String normalizedKeyword = StringUtil.normalizeWhitespace(keyword);
        checkArgument(!normalizedKeyword.isEmpty(), "Role search keyword must not be blank.");
        this.eventId = eventId;
        this.keyword = normalizedKeyword;
        this.includeArchived = includeArchived;
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
                model.updateParticipantRoleSearch(scope, keyword, includeArchived);
            } catch (EventNotFoundException e) {
                throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, scope), e);
            }
        } else {
            model.updateParticipantRoleSearch(keyword, includeArchived);
        }

        int registrationCount = model.getParticipantSearchResults().size();
        if (registrationCount == 0) {
            String message = includeArchived ? MESSAGE_NO_MATCHES_INCLUDING_ARCHIVED : MESSAGE_NO_MATCHES;
            return new CommandResult(String.format(message, keyword), false, false, true);
        }

        int eventCount = model.getFilteredEventList().size();
        return new CommandResult(String.format(MESSAGE_SUCCESS, registrationCount,
                registrationCount == 1 ? "registration" : "registrations", eventCount,
                eventCount == 1 ? "event" : "events", keyword), false, false, true);
    }

    /**
     * Returns whether both commands use the same event scope, normalized query, and archive visibility.
     */
    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchRoleCommand otherCommand
                && eventId.equals(otherCommand.eventId)
                && keyword.equals(otherCommand.keyword)
                && includeArchived == otherCommand.includeArchived;
    }

    /**
     * Returns the hash code of the event scope, normalized query, and archive visibility.
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventId, keyword, includeArchived);
    }
}
