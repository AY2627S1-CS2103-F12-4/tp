package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class CommandSuccessMessagesTest {
    @Test
    public void forCommand_findEvent_includesSearchPhrase() {
        assertEquals("Showing event search results for Workshop...",
                CommandSuccessMessages.forCommand("find-event Workshop"));
        assertEquals("Showing event search results for Welcome Party...",
                CommandSuccessMessages.forCommand(" \tfind-event\t Welcome Party  "));
    }

    @Test
    public void forCommand_mutations_showSpecificSuccessMessages() {
        Map<String, String> messages = Map.ofEntries(
                Map.entry("add", "Person added successfully."),
                Map.entry("edit", "Person updated successfully."),
                Map.entry("delete", "Person deleted successfully."),
                Map.entry("clear", "All people cleared successfully."),
                Map.entry("add-event", "Event added successfully."),
                Map.entry("edit-event", "Event updated successfully."),
                Map.entry("del-event", "Event deleted successfully."),
                Map.entry("add-participant", "Participant added successfully."),
                Map.entry("edit-participant", "Participant updated successfully."),
                Map.entry("del-participant", "Participant removed successfully."),
                Map.entry("mark-participant", "Attendance marked successfully."),
                Map.entry("unmark-participant", "Attendance unmarked successfully."));
        messages.forEach((command, message) ->
                assertEquals(message, CommandSuccessMessages.forCommand(command + " E1 P1")));
    }

    @Test
    public void forCommand_queries_showSpecificViewMessages() {
        Map<String, String> messages = Map.ofEntries(
                Map.entry("list", "Listing people..."),
                Map.entry("find", "Showing person search results..."),
                Map.entry("list-event", "Listing events..."),
                Map.entry("find-event", "Showing event search results..."),
                Map.entry("view-event", "Viewing event..."),
                Map.entry("list-participants", "Listing participants..."),
                Map.entry("view-participant", "Viewing participant..."),
                Map.entry("search-participant", "Showing participant search results..."),
                Map.entry("search-role", "Showing role search results..."),
                Map.entry("help", "Showing help..."),
                Map.entry("exit", "Exiting..."));
        messages.forEach((command, message) -> assertEquals(message, CommandSuccessMessages.forCommand(command)));
    }

    @Test
    public void forCommand_whitespaceAndPrivateArguments_onlyUsesCommandWord() {
        assertEquals("Participant added successfully.",
                CommandSuccessMessages.forCommand(" \tadd-participant\t E1 P1 /role Speaker "));
        String message = CommandSuccessMessages.forCommand("add n/Private Name e/private@example.com");
        assertEquals("Person added successfully.", message);
        assertFalse(message.contains("Private"));
        assertFalse(message.contains("private@example.com"));
    }

    @Test
    public void forCommand_unmappedCommand_usesFallback() {
        assertEquals("Successful!", CommandSuccessMessages.forCommand("new-command"));
        assertThrows(NullPointerException.class, () -> CommandSuccessMessages.forCommand(null));
    }
}
