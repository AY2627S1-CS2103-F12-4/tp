package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;

/** Deletes an event and its participation records. */
public class DeleteEventCommand extends Command {
    public static final String COMMAND_WORD = "del-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Deletes an event and its participations.\n"
            + "Parameters: EVENT_ID\nExample: " + COMMAND_WORD + " E1";
    public static final String MESSAGE_SUCCESS = "Deleted event: %1$s (%2$s)";
    public static final String MESSAGE_EVENT_NOT_FOUND = "Event %s does not exist.";

    private final EventId eventId;

    public DeleteEventCommand(EventId eventId) {
        this.eventId = requireNonNull(eventId);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            Event event = model.deleteEvent(eventId);
            return new CommandResult(String.format(MESSAGE_SUCCESS, event.getName(), event.getId()),
                    false, false, true);
        } catch (EventNotFoundException e) {
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, eventId), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteEventCommand command && eventId.equals(command.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
