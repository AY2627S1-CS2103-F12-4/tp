package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Finds events whose names match the supplied keyword.
 */
public class FindEventCommand extends Command {
    public static final String COMMAND_WORD = "find-event";
    public static final String MESSAGE_NO_MATCHES = "No matching events found.";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds events by name.\n"
            + "Parameters: KEYWORD\n"
            + "Example: " + COMMAND_WORD + " Workshop";

    private final EventNameContainsKeywordPredicate predicate;
    private final boolean includeArchived;

    /**
     * Creates a command with the supplied event-name predicate, excluding archived events.
     */
    public FindEventCommand(EventNameContainsKeywordPredicate predicate) {
        this(predicate, false);
    }

    /**
     * Creates a command with the supplied event-name predicate and archive inclusion setting.
     */
    public FindEventCommand(EventNameContainsKeywordPredicate predicate, boolean includeArchived) {
        this.predicate = requireNonNull(predicate);
        this.includeArchived = includeArchived;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.clearParticipantSearch();
        model.updateFilteredEventList(predicate, includeArchived);
        if (model.getFilteredEventList().isEmpty()) {
            return new CommandResult(MESSAGE_NO_MATCHES, false, false, true);
        }
        return new CommandResult(String.format(
                Messages.MESSAGE_EVENTS_LISTED_OVERVIEW, model.getFilteredEventList().size()), false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof FindEventCommand otherCommand
                && predicate.equals(otherCommand.predicate)
                && includeArchived == otherCommand.includeArchived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate, includeArchived);
    }
}
