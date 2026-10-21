package seedu.address.logic.commands;

import seedu.address.model.event.EventId;

/** Restores an archived event using its stable ID. */
public class UnarchiveEventCommand extends ArchiveEventCommand {
    public static final String COMMAND_WORD = "unarchive-event";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " EVENT_ID";

    public UnarchiveEventCommand(EventId eventId) {
        super(eventId, false);
    }
}
