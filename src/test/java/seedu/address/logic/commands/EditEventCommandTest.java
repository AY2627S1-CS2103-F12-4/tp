package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonEventStorage;
import seedu.address.testutil.PersonBuilder;

public class EditEventCommandTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_preservesRegistrationsSelectionSearchAndOtherEvents() throws Exception {
        Person person = new PersonBuilder().build();
        AddressBook people = new AddressBook();
        people.addPerson(person);
        EventList events = new EventList();
        Event event = events.createEvent("Party", 10, "Old");
        Event other = events.createEvent("Dinner", 20, "Other");
        ModelManager model = new ModelManager(people, new UserPrefs(), events, Set::of);
        EventId id = new EventId(event.getId());
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Speaker"));
        Set<Tag> tags = Set.of(new Tag("VIP"));
        model.addParticipation(id, person.getId(), roles, tags);
        model.markPresent(id, person.getId());
        model.selectEvent(id);
        model.updateParticipantSearch(id, person.getName().fullName);
        model.updateFilteredEventList(candidate -> candidate.getId().equals("E1"));

        CommandResult result = new EditEventCommand(new EventId("E1"), "Renamed", 1, "").execute(model);

        Event updated = new Event(id, "Renamed", 1, "");
        assertEquals(String.format(EditEventCommand.MESSAGE_SUCCESS, "Renamed", id), result.getFeedbackToUser());
        assertEquals(List.of(updated, other), model.getEvents());
        assertEquals(List.of(updated), model.getFilteredEventList());
        assertEquals(updated, model.getSelectedEvent().orElseThrow());
        assertEquals(Set.of(new Participation(updated, person, true, tags, roles)),
                model.getParticipations().getParticipations());
        assertEquals(updated, model.getParticipantSearchEvent().orElseThrow());
        assertEquals(updated, model.getParticipantSearchResults().get(0).getEvent());
        assertEquals(person.getName().fullName, model.getParticipantSearchKeyword().orElseThrow());
        assertEquals("E3", model.createEvent("Next", 1, "").getId());
    }

    @Test
    public void execute_capacityBelowRegistrationCount_preservesAllParticipants() throws Exception {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Party", 10, "");
        for (String personId : List.of("P1", "P2")) {
            Person person = new PersonBuilder().withId(personId).withName("Guest " + personId).build();
            model.addPerson(person);
            model.addParticipation(new EventId(event.getId()), person.getId());
        }
        CommandResult result = new EditEventCommand(new EventId("E1"), "Party", 1, "New").execute(model);
        assertEquals(String.format(EditEventCommand.MESSAGE_SUCCESS, "Party", "E1")
                + String.format(EditEventCommand.MESSAGE_OVERCAPACITY, 2, 1), result.getFeedbackToUser());
        assertEquals(2, model.getParticipationsForEvent(event).size());
        assertEquals(1, model.getEvents().get(0).getCapacity());

        // Further edits must continue to alert the organiser while the event remains overbooked.
        CommandResult renamed = new EditEventCommand(new EventId("E1"), "Renamed", null, null).execute(model);
        assertEquals(String.format(EditEventCommand.MESSAGE_SUCCESS, "Renamed", "E1")
                + String.format(EditEventCommand.MESSAGE_OVERCAPACITY, 2, 1), renamed.getFeedbackToUser());

        for (int newCapacity : List.of(2, 3)) {
            CommandResult increased = new EditEventCommand(new EventId("E1"), null, newCapacity, null).execute(model);
            assertEquals(String.format(EditEventCommand.MESSAGE_SUCCESS, "Renamed", "E1"),
                    increased.getFeedbackToUser());
            assertEquals(2, model.getParticipationsForEvent(model.getEvents().get(0)).size());
        }
    }

    @Test
    public void execute_otherEventSelected_leavesSelectionUnchanged() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "");
        Event other = model.createEvent("Dinner", 20, "");
        model.selectEvent(new EventId(other.getId()));
        new EditEventCommand(new EventId("E1"), "Party", 5, "New").execute(model);
        assertEquals(other, model.getSelectedEvent().orElseThrow());
    }

    @Test
    public void execute_hiddenEvent_updatesWithoutResettingFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        model.updateFilteredEventList(unused -> false);
        new EditEventCommand(new EventId("E1"), "Party", 5, "New").execute(model);
        assertEquals(List.of(), model.getFilteredEventList());
        assertEquals(5, model.getEvents().get(0).getCapacity());
        assertFalse(model.getSelectedEvent().isPresent());
    }

    @Test
    public void execute_renameIntoExistingName_succeedsAndRefreshesNameFilter() throws Exception {
        ModelManager model = new ModelManager();
        Event first = model.createEvent("Party", 10, "First");
        model.createEvent("Dinner", 20, "Second");
        model.updateFilteredEventList(event -> event.getName().equals("Party"));

        new EditEventCommand(new EventId("E2"), "Party", null, null).execute(model);

        Event renamed = new Event(new EventId("E2"), "Party", 20, "Second");
        assertEquals(List.of(first, renamed), model.getFilteredEventList());
        new EditEventCommand(new EventId("E1"), "Other", null, null).execute(model);
        assertEquals(List.of(renamed), model.getFilteredEventList());
    }

    @Test
    public void execute_missingId_failsWithoutChanges() {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        assertCommandFailure(new EditEventCommand(new EventId("E99"), null, 5, null), model,
                String.format(EditEventCommand.MESSAGE_NOT_FOUND, "E99"));
    }

    @Test
    public void execute_duplicateNames_updatesOnlySpecifiedId() throws Exception {
        ModelManager model = new ModelManager();
        Event first = model.createEvent("Party", 10, "First");
        model.createEvent("Party", 20, "Second");
        new EditEventCommand(new EventId("E2"), "Renamed", null, null).execute(model);
        assertEquals(List.of(first, new Event(new EventId("E2"), "Renamed", 20, "Second")), model.getEvents());
    }

    @Test
    public void execute_allNonemptyFieldCombinations_preservesOmittedFields() throws Exception {
        for (int mask = 1; mask < 8; mask++) {
            ModelManager model = new ModelManager();
            model.createEvent("Party", 10, "Old");
            String name = (mask & 1) != 0 ? "New Name" : null;
            Integer capacity = (mask & 2) != 0 ? 20 : null;
            String description = (mask & 4) != 0 ? "" : null;
            new EditEventCommand(new EventId("E1"), name, capacity, description).execute(model);
            assertEquals(new Event(new EventId("E1"), name == null ? "Party" : name,
                    capacity == null ? 10 : capacity, description == null ? "Old" : description),
                    model.getEvents().get(0));
        }
    }

    @Test
    public void execute_unchangedDetails_succeedsAndSurvivesStorageRoundTrip() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        new EditEventCommand(new EventId("E1"), "Party", 10, "Old").execute(model);
        new EditEventCommand(new EventId("E1"), "Renamed", 20, "New").execute(model);
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("events.json"));
        storage.saveEvents(model.getEvents());
        assertEquals(model.getEvents(), storage.readEvents().orElseThrow().getEvents());
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        EventId id = new EventId("E1");
        assertThrows(NullPointerException.class, () -> new EditEventCommand(null, "Name", null, null));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand(id, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand(id, " ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand(id, null, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand(id, null, -1, null));
        assertThrows(NullPointerException.class, () -> new EditEventCommand(id, null, 1, null).execute(null));
    }

    @Test
    public void equalsAndHashCode_comparesAllFields() {
        EditEventCommand command = new EditEventCommand(new EventId("E1"), "Party", 1, "Old");
        assertEquals(command, command);
        assertEquals(command, new EditEventCommand(new EventId("E1"), "Party", 1, "Old"));
        assertEquals(command.hashCode(), new EditEventCommand(new EventId("E1"), "Party", 1, "Old").hashCode());
        assertFalse(command.equals(new EditEventCommand(new EventId("E2"), "Party", 1, "Old")));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), null, 1, null)));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), "Party", null, "Old")));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), "Party", 1, null)));
        assertEquals(new EditEventCommand(new EventId("E1"), null, 1, null),
                new EditEventCommand(new EventId("E1"), null, 1, null));
        assertEquals(new EditEventCommand(new EventId("E1"), null, 1, null).hashCode(),
                new EditEventCommand(new EventId("E1"), null, 1, null).hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("Party"));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), "Other", 1, "Old")));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), "Party", 2, "Old")));
        assertFalse(command.equals(new EditEventCommand(new EventId("E1"), "Party", 1, "New")));
    }
}
