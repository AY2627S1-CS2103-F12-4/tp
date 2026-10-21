package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonEventStorage;
import seedu.address.testutil.PersonBuilder;

public class ArchiveEventCommandTest {
    @TempDir
    public Path testFolder;

    @Test
    public void archiveAndRestore_preservesDetailsMembershipAndSearch() throws Exception {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Welcome Party", 10, "Description");
        Event other = model.createEvent("Other", 20, "");
        EventId id = new EventId(event.getId());
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        Set<Tag> tags = Set.of(new Tag("VIP"));
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Speaker"));
        model.addParticipation(id, person.getId(), roles, tags);
        model.markPresent(id, person.getId());
        model.selectEvent(id);
        model.updateParticipantSearch(id, person.getName().fullName);
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("archive-event E1").execute(model);

        Event archived = event.withArchived(true);
        assertEquals(List.of(archived, other), model.getEvents());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertEquals(archived, model.getSelectedEvent().orElseThrow());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertEquals(Set.of(new Participation(archived, person, true, tags, roles)),
                model.getParticipations().getParticipations());
        assertEquals("Welcome Party <archived>", archived.getDisplayName());
        assertEquals("Welcome Party", archived.getName());
        assertTrue(new ViewEventCommand(id).execute(model).getFeedbackToUser().contains("<archived>"));
        new EditEventCommand(id, "Renamed", null, null).execute(model);
        assertTrue(model.getEvents().get(0).isArchived());
        parser.parseCommand("unarchive-event E1").execute(model);
        assertFalse(model.getEvents().get(0).isArchived());
        assertEquals("Renamed", model.getEvents().get(0).getDisplayName());
        assertEquals(List.of(model.getEvents().getFirst()), model.getFilteredEventList());
        assertEquals("E3", model.createEvent("Next", 1, "").getId());
    }

    @Test
    public void execute_failures_leaveModelUnchanged() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "");
        assertCommandFailure(new ArchiveEventCommand(new EventId("E99")), model,
                String.format(ArchiveEventCommand.MESSAGE_NOT_FOUND, "E99"));
        assertCommandFailure(new ArchiveEventCommand(new EventId("E98")), model,
                String.format(ArchiveEventCommand.MESSAGE_NOT_FOUND, "E98"));
        assertCommandFailure(new UnarchiveEventCommand(new EventId("E1")), model,
                String.format(ArchiveEventCommand.MESSAGE_NOT_ARCHIVED, "E1"));
        new ArchiveEventCommand(new EventId("E1")).execute(model);
        assertCommandFailure(new ArchiveEventCommand(new EventId("E1")), model,
                String.format(ArchiveEventCommand.MESSAGE_ALREADY_ARCHIVED, "E1"));
        model.createEvent("Party", 20, "Duplicate");
        model.updateFilteredEventList(event -> event.getId().equals("E1"));
        new UnarchiveEventCommand(new EventId("E1")).execute(model);
        new ArchiveEventCommand(new EventId("E2")).execute(model);
        assertFalse(model.getEvents().get(0).isArchived());
        assertTrue(model.getEvents().get(1).isArchived());
    }

    @Test
    public void archive_listAndFindHideEvent_restoreRespectsCurrentSearch() throws Exception {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Party", 10, "");
        model.createEvent("Dinner", 20, "");
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("archive-event E1").execute(model);
        parser.parseCommand("list-event").execute(model);
        assertEquals(1, model.getFilteredEventList().size());
        parser.parseCommand("find-event Party").execute(model);
        assertTrue(model.getFilteredEventList().isEmpty());
        parser.parseCommand("unarchive-event E1").execute(model);
        assertEquals(List.of(event), model.getFilteredEventList());
        parser.parseCommand("archive-event E1").execute(model);
        parser.parseCommand("find-event Dinner").execute(model);
        parser.parseCommand("unarchive-event E1").execute(model);
        assertEquals("Dinner", model.getFilteredEventList().get(0).getName());
        assertEquals(1, model.getFilteredEventList().size());
        parser.parseCommand("list-event").execute(model);
        assertEquals(2, model.getFilteredEventList().size());
    }

    @Test
    public void loadedArchivedEvent_staysHiddenAfterFilterResetAndEdits() throws Exception {
        Event archived = new Event(new EventId("E1"), "Party", 10, "", true);
        seedu.address.model.event.EventList events = new seedu.address.model.event.EventList();
        events.addEvent(archived);
        ModelManager model = new ModelManager(new seedu.address.model.AddressBook(),
                new seedu.address.model.UserPrefs(), events);
        assertTrue(model.getFilteredEventList().isEmpty());
        model.updateFilteredEventList(unused -> true);
        assertTrue(model.getFilteredEventList().isEmpty());
        new EditEventCommand(new EventId("E1"), "Renamed", null, null).execute(model);
        assertTrue(model.getFilteredEventList().isEmpty());
        new AddEventCommand("New", 1, "").execute(model);
        assertEquals(1, model.getFilteredEventList().size());
        assertEquals(2, model.getEvents().size());
        new UnarchiveEventCommand(new EventId("E1")).execute(model);
        assertEquals(2, model.getFilteredEventList().size());
    }

    @Test
    public void execute_hiddenEvent_preservesFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "");
        model.updateFilteredEventList(unused -> false);
        new ArchiveEventCommand(new EventId("E1")).execute(model);
        assertTrue(model.getFilteredEventList().isEmpty());
        assertTrue(model.getEvents().get(0).isArchived());
    }

    @Test
    public void storage_preservesArchiveStateAndLoadsLegacyData() throws Exception {
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("events.json"));
        Event event = new Event(new EventId("E1"), "Party", 10, "");
        storage.saveEvents(List.of(event.withArchived(true),
                new Event(new EventId("E2"), "Other", 20, "")));
        assertTrue(storage.readEvents().orElseThrow().getEvents().get(0).isArchived());
        assertFalse(storage.readEvents().orElseThrow().getEvents().get(1).isArchived());
        storage.saveEvents(List.of(event));
        assertFalse(storage.readEvents().orElseThrow().getEvents().get(0).isArchived());
        Files.writeString(testFolder.resolve("events.json"),
                "{\"events\":[{\"id\":\"E1\",\"name\":\"Party\",\"capacity\":10,\"description\":\"\"}]}");
        assertEquals(List.of(event), storage.readEvents().orElseThrow().getEvents());
    }

    @Test
    public void parseAndConstructor_validateNamesAndEquality() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        assertEquals(new ArchiveEventCommand(new EventId("E1")), parser.parseCommand("archive-event  E1  "));
        assertEquals(new UnarchiveEventCommand(new EventId("E1")), parser.parseCommand("unarchive-event E1"));
        assertThrows(ParseException.class, () -> parser.parseCommand("archive-event   "));
        assertThrows(ParseException.class, () -> parser.parseCommand("unarchive-event   "));
        assertThrows(NullPointerException.class, () -> new ArchiveEventCommand(null));
        for (String invalid : List.of("Party", "e1", "E", "E-1", "E1 E2")) {
            assertThrows(ParseException.class, () -> parser.parseCommand("archive-event " + invalid));
            assertThrows(ParseException.class, () -> parser.parseCommand("unarchive-event " + invalid));
        }
        assertThrows(NullPointerException.class, () -> new ArchiveEventCommand(new EventId("E1")).execute(null));
        ArchiveEventCommand command = new ArchiveEventCommand(new EventId("E1"));
        assertEquals(command, command);
        assertEquals(command.hashCode(), new ArchiveEventCommand(new EventId("E1")).hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("Party"));
        assertFalse(command.equals(new ArchiveEventCommand(new EventId("E2"))));
        assertFalse(command.equals(new UnarchiveEventCommand(new EventId("E1"))));
        Event event = new Event(new EventId("E1"), "Party", 10, "");
        assertFalse(event.equals(event.withArchived(true)));
        assertTrue(event.isSameEvent(event.withArchived(true)));
        assertEquals(event.withArchived(true).hashCode(), event.withArchived(true).hashCode());
    }
}
