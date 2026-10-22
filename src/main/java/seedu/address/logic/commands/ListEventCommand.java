package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.model.Model;

/** Lists events with optional archive inclusion, replacing any active event filter. */
public class ListEventCommand extends Command {
    public static final String COMMAND_WORD = "list-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists nonarchived events, or all events with /include-archive.\n"
            + "Parameters: [/include-archive]\nExample: " + COMMAND_WORD + " /include-archive";
    public static final String MESSAGE_NO_EVENTS = "No events found.";

    private final boolean includeArchived;

    /** Creates an event listing that excludes archived events. */
    public ListEventCommand() {
        this(false);
    }

    /** Creates an event listing with the requested archive visibility. */
    public ListEventCommand(boolean includeArchived) {
        this.includeArchived = includeArchived;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.showEvents(includeArchived);
        int count = model.getFilteredEventList().size();
        String message = count == 0 ? MESSAGE_NO_EVENTS
                : String.format(Messages.MESSAGE_EVENTS_LISTED_OVERVIEW, count);
        return new CommandResult(message, false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof ListEventCommand otherCommand
                && includeArchived == otherCommand.includeArchived;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(includeArchived);
    }
}
