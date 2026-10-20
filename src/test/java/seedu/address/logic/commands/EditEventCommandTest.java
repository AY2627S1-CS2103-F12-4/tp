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
        model.updateFilteredEventList(candidate -> candidate.getName().equals("Party"));

        CommandResult result = new EditEventCommand("Party", 1, "").execute(model);

        Event updated = new Event(id, "Party", 1, "");
        assertEquals(String.format(EditEventCommand.MESSAGE_SUCCESS, "Party", id), result.getFeedbackToUser());
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
        new EditEventCommand("Party", 1, "New").execute(model);
        assertEquals(2, model.getParticipationsForEvent(event).size());
        assertEquals(1, model.getEvents().get(0).getCapacity());
    }

    @Test
    public void execute_otherEventSelected_leavesSelectionUnchanged() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "");
        Event other = model.createEvent("Dinner", 20, "");
        model.selectEvent(new EventId(other.getId()));
        new EditEventCommand("Party", 5, "New").execute(model);
        assertEquals(other, model.getSelectedEvent().orElseThrow());
    }

    @Test
    public void execute_hiddenEvent_updatesWithoutResettingFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        model.updateFilteredEventList(unused -> false);
        new EditEventCommand("Party", 5, "New").execute(model);
        assertEquals(List.of(), model.getFilteredEventList());
        assertEquals(5, model.getEvents().get(0).getCapacity());
        assertFalse(model.getSelectedEvent().isPresent());
    }

    @Test
    public void execute_missingOrDifferentCaseName_failsWithoutChanges() {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        for (String name : List.of("Missing", "party")) {
            assertCommandFailure(new EditEventCommand(name, 5, "New"), model,
                    String.format(EditEventCommand.MESSAGE_NOT_FOUND, name));
        }
    }

    @Test
    public void execute_duplicateNames_failsEvenWhenOneIsHidden() {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "First");
        model.createEvent("Party", 20, "Second");
        model.updateFilteredEventList(event -> event.getId().equals("E1"));
        assertCommandFailure(new EditEventCommand("Party", 5, "New"), model,
                String.format(EditEventCommand.MESSAGE_AMBIGUOUS, "Party"));
    }

    @Test
    public void execute_unchangedDetails_succeedsAndSurvivesStorageRoundTrip() throws Exception {
        ModelManager model = new ModelManager();
        model.createEvent("Party", 10, "Old");
        new EditEventCommand("Party", 10, "Old").execute(model);
        new EditEventCommand("Party", 20, "New").execute(model);
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("events.json"));
        storage.saveEvents(model.getEvents());
        assertEquals(model.getEvents(), storage.readEvents().orElseThrow().getEvents());
    }

    @Test
    public void constructor_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new EditEventCommand(null, 1, ""));
        assertThrows(NullPointerException.class, () -> new EditEventCommand("Party", 1, null));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand(" ", 1, ""));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand("Party", 0, ""));
        assertThrows(IllegalArgumentException.class, () -> new EditEventCommand("Party", -1, ""));
        assertThrows(NullPointerException.class, () -> new EditEventCommand("Party", 1, "").execute(null));
    }

    @Test
    public void equalsAndHashCode_comparesAllFields() {
        EditEventCommand command = new EditEventCommand("Party", 1, "Old");
        assertEquals(command, command);
        assertEquals(command, new EditEventCommand("Party", 1, "Old"));
        assertEquals(command.hashCode(), new EditEventCommand("Party", 1, "Old").hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("Party"));
        assertFalse(command.equals(new EditEventCommand("Other", 1, "Old")));
        assertFalse(command.equals(new EditEventCommand("Party", 2, "Old")));
        assertFalse(command.equals(new EditEventCommand("Party", 1, "New")));
    }
}
