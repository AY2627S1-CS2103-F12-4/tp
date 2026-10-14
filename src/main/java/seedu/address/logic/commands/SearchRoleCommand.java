package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRoleContainsKeywordPredicate;

/**
 * Searches participation roles within one event without modifying data or display filters.
 * Results contain each matching person's ID, full name, and all assigned roles.
 */
public class SearchRoleCommand extends Command {
    public static final String COMMAND_WORD = "search-role";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Searches participant roles within an event using a case-insensitive substring.\n"
            + "Parameters: EVENT_ID /role KEYWORD\n"
            + "Example: " + COMMAND_WORD + " E1 /role Best Man";
    public static final String MESSAGE_SUCCESS =
            "%d participants with roles matching \"%s\" found in %s.";
    public static final String MESSAGE_NO_MATCHES =
            "No participants with roles matching \"%s\" were found in %s.";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";

    private final EventId eventId;
    private final String keyword;
    private final ParticipationRoleContainsKeywordPredicate predicate;

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
        requireAllNonNull(eventId, keyword);
        this.predicate = new ParticipationRoleContainsKeywordPredicate(keyword);
        this.eventId = eventId;
        this.keyword = keyword.trim().replaceAll("\\s+", " ");
    }

    /**
     * Returns matching participants in ascending string order of person ID, with roles sorted by name.
     * Multiple matching roles do not repeat a participant, and equal names do not merge distinct IDs.
     *
     * @throws CommandException if the event does not exist or cannot be retrieved from the mapper.
     * @throws NullPointerException if {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event event = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId)));

        List<Participation> matches;
        try {
            matches = model.getParticipationsForEvent(event).stream()
                    .filter(predicate)
                    .sorted(Comparator.comparing(participation -> participation.getPerson().getId().toString()))
                    .toList();
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        }

        if (matches.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_MATCHES, keyword, event.getName()));
        }

        String details = matches.stream()
                .map(participation -> participation.getPerson().getId()
                        + " — " + participation.getPerson().getName()
                        + " — " + participation.getRoles().stream()
                        .map(Object::toString)
                        .sorted()
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_SUCCESS, matches.size(), keyword, event.getName())
                + "\n" + details);
    }

    /**
     * Returns whether both commands search the same event and have identical normalized feedback queries.
     */
    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof SearchRoleCommand otherCommand
                && eventId.equals(otherCommand.eventId)
                && keyword.equals(otherCommand.keyword);
    }

    /**
     * Returns the hash code of the event ID and whitespace-normalized query.
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventId, keyword);
    }
}
