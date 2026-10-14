package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class ViewEventCommandTest {
    @Test
    public void execute_hiddenEvent_selectsByIdAndDisplaysDetailsWithoutChangingFilter() throws Exception {
        ModelManager model = new ModelManager();
        Event first = model.createEvent("Workshop", 10, "Learn Java");
        Event second = model.createEvent("Concert", 20, "Music");
        model.updateFilteredEventList(event -> event.equals(second));
        assertTrue(model.getSelectedEvent().isEmpty());

        CommandResult result = new ViewEventCommand(new EventId(first.getId())).execute(model);

        assertEquals(Optional.of(first), model.getSelectedEvent());
        assertEquals(List.of(second), model.getFilteredEventList());
        assertEquals(List.of(first, second), model.getEvents());
        assertEquals(new CommandResult("Event Details\nEvent ID: E1\nName: Workshop\nCapacity: 10\n"
                + "Description: Learn Java\n\nNo participants found in Workshop.", false, false, true), result);
    }

    @Test
    public void execute_participants_displaysOnlySelectedEventsParticipantsInStableOrder() throws Exception {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Workshop", 10, "");
        Event other = model.createEvent("Other", 1, "");
        var alice = new PersonBuilder().withId("P1").withName("Alice").build();
        var bob = new PersonBuilder().withId("P2").withName("Bob").build();
        var carol = new PersonBuilder().withId("P3").withName("Carol").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(carol);
        model.addParticipation(event, bob);
        model.addParticipation(event, alice);
        model.addParticipation(other, carol);
        var before = model.getParticipations().getParticipations();
        String feedback = new ViewEventCommand(new EventId("E1")).execute(model).getFeedbackToUser();
        assertTrue(feedback.contains("2 participants found in Workshop.\nP1 — Alice — No role\nP2 — Bob — No role"));
        assertFalse(feedback.contains("Carol"));
        assertEquals(before, model.getParticipations().getParticipations());
        new ViewEventCommand(new EventId("E2")).execute(model);
        assertEquals(Optional.of(other), model.getSelectedEvent());
    }

    @Test
    public void execute_missingEvent_preservesPreviousSelectionAndFilter() throws Exception {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Workshop", 1, "");
        new ViewEventCommand(new EventId("E1")).execute(model);
        model.updateFilteredEventList(unused -> false);
        CommandException exception = assertThrows(CommandException.class, () ->
                new ViewEventCommand(new EventId("E99")).execute(model));
        assertEquals("Event E99 does not exist.", exception.getMessage());
        assertEquals(Optional.of(event), model.getSelectedEvent());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_emptyModel_reportsMissingEvent() {
        ModelManager model = new ModelManager();
        assertThrows(CommandException.class, () -> new ViewEventCommand(new EventId("E1")).execute(model));
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void selectEvent_invalidId_preservesSelection() {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Workshop", 1, "");
        model.selectEvent(new EventId("E1"));
        assertThrows(EventNotFoundException.class, () -> model.selectEvent(new EventId("E2")));
        assertThrows(NullPointerException.class, () -> model.selectEvent(null));
        assertEquals(Optional.of(event), model.getSelectedEvent());
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewEventCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewEventCommand(new EventId("E1")).execute(null));
    }

    @Test
    public void deleteEvent_clearsSelectionOnlyWhenSelectedEventIsDeleted() throws Exception {
        ModelManager model = new ModelManager();
        Event selected = model.createEvent("Selected", 1, "");
        model.createEvent("Other", 1, "");
        new ViewEventCommand(new EventId("E1")).execute(model);
        new DeleteEventCommand(new EventId("E2")).execute(model);
        assertEquals(Optional.of(selected), model.getSelectedEvent());
        assertThrows(CommandException.class, () -> new DeleteEventCommand(new EventId("E99")).execute(model));
        assertEquals(Optional.of(selected), model.getSelectedEvent());
        new DeleteEventCommand(new EventId("E1")).execute(model);
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void equalsAndHashCode_compareEventIds() {
        ViewEventCommand command = new ViewEventCommand(new EventId("E1"));
        ViewEventCommand copy = new ViewEventCommand(new EventId("E1"));
        assertTrue(command.equals(command));
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(new ViewEventCommand(new EventId("E2"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("E1"));
    }
}
