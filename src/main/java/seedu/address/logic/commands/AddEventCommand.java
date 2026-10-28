package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Creates an event in the model. */
public class AddEventCommand extends Command {
    public static final String COMMAND_WORD = "add-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Creates an event. Parameters: EVENT_NAME /cap CAPACITY /desc DESCRIPTION\n"
            + "Example: add-event Orientation /cap 100 /desc Welcome session";
    public static final String MESSAGE_SUCCESS = "New event added: %1$s (%2$s)";
    public static final String MESSAGE_INVALID_CAPACITY = "Event capacity must be a positive integer";

    private final String name;
    private final int capacity;
    private final String description;

    /** Validates the event details before accepting the command. */
    public AddEventCommand(String name, int capacity, String description) {
        // Validate using the event's existing constraints; the model assigns the actual ID on execution.
        new Event(new EventId("E1"), name, capacity, description);
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Event event = model.createEvent(name, capacity, description);
        if (model.getParticipantSearchKeyword().isEmpty()) {
            model.updateFilteredEventList(unused -> true);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, event.getName(), event.getId()), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddEventCommand command
                && name.equals(command.name)
                && capacity == command.capacity
                && description.equals(command.description);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, capacity, description);
    }
}
