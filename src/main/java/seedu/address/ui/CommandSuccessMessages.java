package seedu.address.ui;

import static java.util.Objects.requireNonNull;

/** Short status messages shown after commands complete successfully. */
public final class CommandSuccessMessages {
    private CommandSuccessMessages() {
    }

    /** Returns a concise status without including command results or personal details. */
    public static String forCommand(String commandText) {
        String commandWord = requireNonNull(commandText).strip().split("\\s+", 2)[0];
        return switch (commandWord) {
            case "add" -> "Person added successfully.";
            case "edit" -> "Person updated successfully.";
            case "delete" -> "Person deleted successfully.";
            case "clear" -> "All people cleared successfully.";
            case "list" -> "Listing people...";
            case "find" -> "Showing person search results...";
            case "add-event" -> "Event added successfully.";
            case "edit-event" -> "Event updated successfully.";
            case "del-event" -> "Event deleted successfully.";
            case "list-event" -> "Listing events...";
            case "find-event" -> "Showing event search results...";
            case "view-event" -> "Viewing event...";
            case "add-participant" -> "Participant added successfully.";
            case "edit-participant" -> "Participant updated successfully.";
            case "del-participant" -> "Participant removed successfully.";
            case "list-participants" -> "Listing participants...";
            case "view-participant" -> "Viewing participant...";
            case "search-participant" -> "Showing participant search results...";
            case "search-role" -> "Showing role search results...";
            case "mark-participant" -> "Attendance marked successfully.";
            case "unmark-participant" -> "Attendance unmarked successfully.";
            case "help" -> "Showing help...";
            case "exit" -> "Exiting...";
            default -> "Successful!";
        };
    }
}
