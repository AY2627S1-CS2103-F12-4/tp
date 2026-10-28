package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;

/**
 * Lists all participants registered for an event.
 */
public class ListParticipantsCommand extends Command {

    public static final String COMMAND_WORD = "list-participants";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists participants of an event.\n"
            + "Parameters: EVENT_ID\n"
            + "Example: " + COMMAND_WORD + " E1";

    public static final String MESSAGE_EVENT_NOT_FOUND =
            "Event %s does not exist.";

    private final EventId eventId;

    public ListParticipantsCommand(EventId eventId) {
        this.eventId = requireNonNull(eventId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Event event = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_EVENT_NOT_FOUND, eventId)));

        Set<Participation> participants;
        try {
            participants = model.getParticipationsForEvent(event);
        } catch (EventNotFoundException e) {
            throw new CommandException(
                    String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        }

        model.selectEvent(eventId);

        if (participants.isEmpty()) {
            return new CommandResult(
                    "No participants found in " + event.getName() + ".",
                    false,
                    false,
                    false,
                    true);
        }

        String details = participants.stream()
                .sorted(Comparator.comparing(
                        participation -> participation.getPerson()
                                .getId()))
                .map(this::formatParticipation)
                .collect(Collectors.joining("\n"));

        String feedback = participants.size()
                + " participants found in "
                + event.getName()
                + ".\n"
                + details;

        return new CommandResult(
                feedback,
                false,
                false,
                false,
                true);
    }

    /**
     * Formats a participation for display in the command result.
     */
    private String formatParticipation(Participation participation) {
        String roles = participation.getRoles().isEmpty()
                ? "No role"
                : participation.getRoles().stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        String tags = participation.getTags().isEmpty()
                ? "None"
                : participation.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));

        return (participation.isPresent() ? "[Y]" : "[N]")
                + participation.getPerson().getId()
                + "-"
                + participation.getPerson().getName()
                + "-roles:"
                + roles
                + "（Tags:"
                + tags
                + "）";
    }
}
