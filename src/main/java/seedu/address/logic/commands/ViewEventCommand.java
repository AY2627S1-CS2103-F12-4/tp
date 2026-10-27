package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Selects an event by stable ID and displays its details and participants. */
public class ViewEventCommand extends Command {
    public static final String COMMAND_WORD = "view-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Views an event's details and participants.\n"
            + "Parameters: EVENT_ID\nExample: " + COMMAND_WORD + " E1";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";

    private final EventId eventId;

    public ViewEventCommand(EventId eventId) {
        this.eventId = requireNonNull(eventId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event event = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId)));
        // Reuse the participant listing without replacing the current event and search filters.
        String participants = new ListParticipantsCommand(eventId, false).execute(model).getFeedbackToUser();
        String details = "Event Details\n"
                + "Event ID: " + event.getId() + "\n"
                + "Name: " + event.getDisplayName() + "\n"
                + "Capacity: " + event.getCapacity() + "\n"
                + "Description: " + event.getDescription() + "\n\n"
                + participants;
        model.selectEvent(eventId);
        return new CommandResult(details, false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ViewEventCommand command && eventId.equals(command.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
