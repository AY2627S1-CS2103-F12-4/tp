package seedu.address.logic.commands;

import seedu.address.model.Model;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Finds events whose names match the supplied keyword.
 */
public class FindEventCommand extends Command {
    public static final String COMMAND_WORD = "find-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds events by name.\n"
            + "Parameters: KEYWORD\n"
            + "Example: " + COMMAND_WORD + " Workshop";

    private final EventNameContainsKeywordPredicate predicate;

    /**
     * Creates a command with the supplied event-name predicate.
     */
    public FindEventCommand(EventNameContainsKeywordPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        // TODO: Drive filtered event-list support in Model with command tests.
        throw new UnsupportedOperationException("Find-event execution is not implemented yet");
    }
}
