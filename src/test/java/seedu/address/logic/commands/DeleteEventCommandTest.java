package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class DeleteEventCommandTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_hiddenEvent_removesOnlyTargetAndItsParticipations() throws Exception {
        ModelManager model = new ModelManager();
        Event target = model.createEvent("Workshop", 10, "");
        Event other = model.createEvent("Concert", 10, "");
        var person = new PersonBuilder().build();
        model.addPerson(person);
        model.addParticipation(target, person);
        model.addParticipation(other, person);
        var remainingParticipation = model.getParticipationsForEvent(other);
        model.updateFilteredEventList(event -> event.equals(other));

        CommandResult result = new DeleteEventCommand(new EventId("E1")).execute(model);

        assertEquals(new CommandResult("Deleted event: Workshop (E1)", false, false, true), result);
        assertEquals(List.of(other), model.getEvents());
        assertEquals(List.of(other), model.getFilteredEventList());
        assertEquals(remainingParticipation, model.getParticipations().getParticipations());
        assertTrue(model.hasPerson(person));
        model.createEvent("Hidden", 1, "");
        assertEquals(List.of(other), model.getFilteredEventList());
    }

    @Test
    public void execute_deleteLastEventThenCreate_doesNotReuseDeletedId() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Workshop", 1, "");
        new DeleteEventCommand(new EventId("E1")).execute(model);
        assertTrue(model.getFilteredEventList().isEmpty());
        assertEquals("E2", model.createEvent("Next", 1, "").getId());
    }

    @Test
    public void execute_missingEvent_leavesDataUnchanged() {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Workshop", 1, "");
        model.updateFilteredEventList(unused -> false);
        CommandException exception = assertThrows(CommandException.class, () ->
                new DeleteEventCommand(new EventId("E99")).execute(model));
        assertEquals("Event E99 does not exist.", exception.getMessage());
        assertEquals(List.of(event), model.getEvents());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_emptyModel_reportsMissingEvent() {
        assertThrows(CommandException.class, () ->
                new DeleteEventCommand(new EventId("E1")).execute(new ModelManager()));
    }

    @Test
    public void executeThroughLogic_deletionPersistsAcrossReload() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        ModelManager model = new ModelManager();
        Event target = model.createEvent("Workshop", 1, "");
        Event remaining = model.createEvent("Concert", 1, "");
        var person = new PersonBuilder().build();
        model.addPerson(person);
        model.addParticipation(target, person);
        model.addParticipation(remaining, person);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("list");
        logic.execute("del-event E1");

        AddressBook people = new AddressBook(storage.readAddressBook().orElseThrow());
        var events = storage.readEvents().orElseThrow();
        var participations = storage.readParticipations(events, people).orElseThrow();
        ModelManager restored = new ModelManager(people, new UserPrefs(), events, participations);
        assertEquals(List.of(remaining), restored.getEvents());
        assertEquals(model.getParticipations().getParticipations(), restored.getParticipations().getParticipations());
        assertTrue(restored.hasPerson(person));
    }

    @Test
    public void equalsAndHashCode_compareEventId() {
        DeleteEventCommand command = new DeleteEventCommand(new EventId("E1"));
        DeleteEventCommand copy = new DeleteEventCommand(new EventId("E1"));
        assertTrue(command.equals(command));
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertFalse(command.equals(new DeleteEventCommand(new EventId("E2"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("E1"));
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteEventCommand(null));
        assertThrows(NullPointerException.class, () -> new DeleteEventCommand(new EventId("E1")).execute(null));
        assertThrows(NullPointerException.class, () -> new ModelManager().deleteEvent(null));
    }
}
