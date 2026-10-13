package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;

/**
 * Searches participants by name within an event identified by its ID.
 */
public class SearchParticipantCommand extends Command {
    public static final String COMMAND_WORD = "search-participant";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Searches an event's participants by name.\n"
            + "Parameters: EVENT_ID /name NAME\n"
            + "Example: " + COMMAND_WORD + " E1 /name Michael";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";
    public static final String MESSAGE_NO_MATCHES = "No participants matching \"%1$s\" were found in %2$s.";
    public static final String MESSAGE_ONE_MATCH = "1 participant matching \"%1$s\" found in %2$s.";
    public static final String MESSAGE_MATCHES = "%1$d participants matching \"%2$s\" found in %3$s.";

    private final EventId eventId;
    private final String keyword;

    /**
     * Creates a search for {@code keyword} within the event identified by {@code eventId}.
     * Surrounding whitespace is removed from the nonblank keyword.
     */
    public SearchParticipantCommand(EventId eventId, String keyword) {
        requireAllNonNull(eventId, keyword);
        checkArgument(!keyword.isBlank(), "Participant search names must not be blank.");
        this.eventId = eventId;
        this.keyword = keyword.strip();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event event = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId)));
        try {
            model.updateParticipantSearch(eventId, keyword);
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        }
        int matchCount = model.getParticipantSearchResults().size();
        if (matchCount == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, keyword, event.getName()),
                    false, false, false, true);
        }
        if (matchCount == 1) {
            return new CommandResult(String.format(MESSAGE_ONE_MATCH, keyword, event.getName()),
                    false, false, false, true);
        }
        return new CommandResult(String.format(MESSAGE_MATCHES, matchCount, keyword, event.getName()),
                false, false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchParticipantCommand otherCommand
                && eventId.equals(otherCommand.eventId)
                && keyword.equals(otherCommand.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, keyword);
    }
}
