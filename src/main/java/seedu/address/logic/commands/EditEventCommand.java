package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Updates an event identified by its exact name in the model. */
public class EditEventCommand extends Command {
    public static final String COMMAND_WORD = "edit-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates an event identified by its exact name.\n"
            + "Parameters: EVENT_NAME /cap CAPACITY /desc DESCRIPTION\n"
            + "Example: edit-event Orientation /cap 100 /desc Welcome session";
    public static final String MESSAGE_SUCCESS = "Event updated: %1$s (%2$s)";
    public static final String MESSAGE_INVALID_CAPACITY = "Event capacity must be a positive integer";

    public static final String MESSAGE_NOT_FOUND = "No event has the name: %s";
    public static final String MESSAGE_AMBIGUOUS =
            "Multiple events have the name: %s. The event cannot be edited by name.";

    private final String name;
    private final int capacity;
    private final String description;

    /** Validates the event details before accepting the command. */
    public EditEventCommand(String name, int capacity, String description) {
        // Validate details before execution; the existing event's ID is retained on update.
        new Event(new EventId("E1"), name, capacity, description);
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Event> matches = model.getEvents().stream()
                .filter(event -> event.getName().equals(name)).toList();
        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, name));
        }
        if (matches.size() > 1) {
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS, name));
        }
        Event current = matches.get(0);
        Event event = new Event(new EventId(current.getId()), name, capacity, description);
        model.updateEvent(event);
        return new CommandResult(String.format(MESSAGE_SUCCESS, event.getName(), event.getId()), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditEventCommand command
                && name.equals(command.name)
                && capacity == command.capacity
                && description.equals(command.description);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, capacity, description);
    }
}
