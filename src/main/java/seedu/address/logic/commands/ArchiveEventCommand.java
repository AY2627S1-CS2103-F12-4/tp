package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Changes archive state without hiding events or restricting their other operations. */
public class ArchiveEventCommand extends Command {
    public static final String COMMAND_WORD = "archive-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " EVENT_ID";
    public static final String MESSAGE_NOT_FOUND = "Event %s does not exist.";
    public static final String MESSAGE_ALREADY_ARCHIVED = "Event is already archived: %s";
    public static final String MESSAGE_NOT_ARCHIVED = "Event is not archived: %s";

    private final EventId eventId;
    private final boolean archived;

    public ArchiveEventCommand(EventId eventId) {
        this(eventId, true);
    }

    /** Shares archive validation and execution with the restore command. */
    protected ArchiveEventCommand(EventId eventId, boolean archived) {
        this.eventId = requireNonNull(eventId);
        this.archived = archived;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Event event = model.getEvents().stream()
                .filter(candidate -> candidate.getId().equals(eventId.toString()))
                .findFirst()
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND, eventId)));
        if (event.isArchived() == archived) {
            throw new CommandException(String.format(
                    archived ? MESSAGE_ALREADY_ARCHIVED : MESSAGE_NOT_ARCHIVED, eventId));
        }
        model.updateEvent(event.withArchived(archived));
        return new CommandResult(archived ? "Event archived successfully." : "Event restored successfully.",
                false, false, true);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ArchiveEventCommand command
                && eventId.equals(command.eventId) && archived == command.archived;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, archived);
    }
}
