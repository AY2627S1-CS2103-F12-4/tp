package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.model.Model;

/** Lists all events, replacing any active event filter. */
public class ListEventCommand extends Command {
    public static final String COMMAND_WORD = "list-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all events.\n"
            + "Parameters: none\nExample: " + COMMAND_WORD;
    public static final String MESSAGE_NO_EVENTS = "No events found.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredEventList(unused -> true);
        int count = model.getFilteredEventList().size();
        String message = count == 0 ? MESSAGE_NO_EVENTS
                : String.format(Messages.MESSAGE_EVENTS_LISTED_OVERVIEW, count);
        return new CommandResult(message, false, false, true);
    }
}
