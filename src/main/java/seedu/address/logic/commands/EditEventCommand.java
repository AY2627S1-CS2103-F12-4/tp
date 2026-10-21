package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Updates an event identified by its ID in the model. */
public class EditEventCommand extends Command {
    public static final String COMMAND_WORD = "edit-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates an event identified by its ID.\n"
            + "Parameters: EVENT_ID [EVENT_NAME] [/cap CAPACITY] [/desc DESCRIPTION]\n"
            + "Example: edit-event E1 Orientation /cap 100 /desc Welcome session";
    public static final String MESSAGE_SUCCESS = "Event updated: %1$s (%2$s)";
    public static final String MESSAGE_INVALID_CAPACITY = "Event capacity must be a positive integer";

    public static final String MESSAGE_NOT_FOUND = "Event %s does not exist.";
    public static final String MESSAGE_NO_CHANGES = "At least one event field must be provided.";

    private final EventId eventId;
    private final String name;
    private final Integer capacity;
    private final String description;

    /** Null fields are omitted; an empty description explicitly clears the description. */
    public EditEventCommand(EventId eventId, String name, Integer capacity, String description) {
        this.eventId = requireNonNull(eventId);
        checkArgument(name != null || capacity != null || description != null, MESSAGE_NO_CHANGES);
        new Event(eventId, name == null ? "Placeholder" : name,
                capacity == null ? 1 : capacity, description == null ? "" : description);
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event current = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND, eventId)));
        Event event = new Event(eventId, name == null ? current.getName() : name,
                capacity == null ? current.getCapacity() : capacity,
                description == null ? current.getDescription() : description);
        model.updateEvent(event);
        return new CommandResult(String.format(MESSAGE_SUCCESS, event.getName(), event.getId()), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EditEventCommand command
                && eventId.equals(command.eventId)
                && Objects.equals(name, command.name)
                && Objects.equals(capacity, command.capacity)
                && Objects.equals(description, command.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, name, capacity, description);
    }
}
