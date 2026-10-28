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
 * Searches participants by name with optional event scope and archive inclusion.
 */
public class SearchParticipantCommand extends Command {
    /** Command word accepted by the address book parser. */
    public static final String COMMAND_WORD = "search-participant";
    /** Expected syntax and an example shown when arguments are invalid. */
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Searches participants by name.\n"
            + "Archived events are excluded unless /include-archive is supplied.\n"
            + "Parameters: NAME [/event EVENTID] [/include-archive]\n"
            + "Example: " + COMMAND_WORD + " Michael Tan /event E1 /include-archive";
    /** Feedback when no event has the supplied ID. */
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";
    /** Feedback for an empty result list. */
    public static final String MESSAGE_NO_MATCHES = "No matching registrations for \"%s\" in nonarchived events.";
    /** Feedback for an empty result list when archived events are included. */
    public static final String MESSAGE_NO_MATCHES_INCLUDING_ARCHIVED =
            "No matching registrations for \"%s\" (including archived events).";
    /** Feedback including registration count, event count, and the normalized search query. */
    public static final String MESSAGE_MATCHES = "%1$d matching %2$s across %3$d %4$s for \"%5$s\".";

    private final Optional<EventId> eventId;
    private final String keyword;
    private final boolean shouldIncludeArchived;

    /**
     * Creates a search for {@code keyword} within the event identified by {@code eventId}.
     * Whitespace is normalized in the nonblank keyword.
     *
     * @param eventId Stable ID of the event to search.
     * @param keyword Nonblank name phrase to match.
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the keyword is blank.
     */
    public SearchParticipantCommand(EventId eventId, String keyword) {
        this(eventId, keyword, false);
    }

    /** Creates a scoped name search with the requested archive visibility. */
    public SearchParticipantCommand(EventId eventId, String keyword, boolean shouldIncludeArchived) {
        this(Optional.of(eventId), keyword, shouldIncludeArchived);
    }

    /** Creates a name search across all nonarchived events. */
    public SearchParticipantCommand(String keyword) {
        this(keyword, false);
    }

    /** Creates a global name search with the requested archive visibility. */
    public SearchParticipantCommand(String keyword, boolean shouldIncludeArchived) {
        this(Optional.empty(), keyword, shouldIncludeArchived);
    }

    private SearchParticipantCommand(Optional<EventId> eventId, String keyword, boolean shouldIncludeArchived) {
        String normalizedKeyword = StringUtil.normalizeWhitespace(keyword);
        checkArgument(!normalizedKeyword.isEmpty(), "Participant search names must not be blank.");
        this.eventId = eventId;
        this.keyword = normalizedKeyword;
        this.shouldIncludeArchived = shouldIncludeArchived;
    }

    /**
     * Replaces search results and event filters independently of existing display filters.
     * Counts registrations separately when one person belongs to multiple matching events.
     *
     * @param model Model containing the events and participation records.
     * @return Registration and event counts with an instruction to show the Events tab.
     * @throws NullPointerException if the model is null.
     * @throws CommandException if the event does not exist.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            if (eventId.isPresent()) {
                model.updateParticipantSearch(eventId.orElseThrow(), keyword, shouldIncludeArchived);
            } else {
                model.updateParticipantSearch(keyword, shouldIncludeArchived);
            }
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId.orElseThrow()), e);
        }
        int matchCount = model.getParticipantSearchResults().size();
        if (matchCount == 0) {
            String message = shouldIncludeArchived ? MESSAGE_NO_MATCHES_INCLUDING_ARCHIVED : MESSAGE_NO_MATCHES;
            return new CommandResult(String.format(message, keyword), false, false, true);
        }
        int eventCount = model.getFilteredEventList().size();
        return new CommandResult(String.format(MESSAGE_MATCHES, matchCount,
                matchCount == 1 ? "registration" : "registrations", eventCount,
                eventCount == 1 ? "event" : "events", keyword), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchParticipantCommand otherCommand
                && eventId.equals(otherCommand.eventId)
                && keyword.equals(otherCommand.keyword)
                && shouldIncludeArchived == otherCommand.shouldIncludeArchived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, keyword, shouldIncludeArchived);
    }
}
