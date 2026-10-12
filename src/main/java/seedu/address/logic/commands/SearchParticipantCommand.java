package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.ParticipantNameContainsKeywordPredicate;

/**
 * Searches the participants of a displayed event by name.
 */
public class SearchParticipantCommand extends Command {
    public static final String COMMAND_WORD = "search-participant";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Searches an event's participants by name.\n"
            + "Parameters: EVENT_INDEX /name NAME\n"
            + "Example: " + COMMAND_WORD + " 1 /name Michael";
    public static final String MESSAGE_INVALID_EVENT_INDEX = "The event index provided is invalid.";
    public static final String MESSAGE_NO_MATCHES = "No participants matching \"%1$s\" were found in %2$s.";
    public static final String MESSAGE_ONE_MATCH = "1 participant matching \"%1$s\" found in %2$s.";
    public static final String MESSAGE_MATCHES = "%1$d participants matching \"%2$s\" found in %3$s.";

    private final Index eventIndex;
    private final String keyword;

    /**
     * Creates a search for {@code keyword} within the event at {@code eventIndex}.
     * Surrounding whitespace is removed from the nonblank keyword.
     */
    public SearchParticipantCommand(Index eventIndex, String keyword) {
        requireAllNonNull(eventIndex, keyword);
        checkArgument(!keyword.isBlank(), "Participant search names must not be blank.");
        this.eventIndex = eventIndex;
        this.keyword = keyword.strip();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Event> displayedEvents = model.getFilteredEventList();
        if (eventIndex.getZeroBased() >= displayedEvents.size()) {
            throw new CommandException(MESSAGE_INVALID_EVENT_INDEX);
        }

        Event event = displayedEvents.get(eventIndex.getZeroBased());
        long matchCount = model.getParticipationsForEvent(event).stream()
                .filter(new ParticipantNameContainsKeywordPredicate(keyword))
                .count();
        if (matchCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, keyword, event.getName()));
        }
        if (matchCount == 1) {
            return new CommandResult(String.format(MESSAGE_ONE_MATCH, keyword, event.getName()));
        }
        return new CommandResult(String.format(MESSAGE_MATCHES, matchCount, keyword, event.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchParticipantCommand otherCommand
                && eventIndex.equals(otherCommand.eventIndex)
                && keyword.equals(otherCommand.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventIndex, keyword);
    }
}
