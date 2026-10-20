package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.testutil.PersonBuilder;

public class EditParticipantCommandTest {


    private static final EventId EVENT_ID = new EventId("E1");
    private static final PersonId PERSON_ID = new PersonId("P1");
    private static final Event EVENT = new Event(EVENT_ID, "Workshop", 10, "Learn Java");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();

    private static final Set<ParticipationRole> ROLES = Set.of(new ParticipationRole("Speaker"));

    @TempDir
    public java.nio.file.Path testFolder;

    private ModelManager model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(PERSON);
        EventList eventList = new EventList();
        eventList.addEvent(EVENT);
        model = new ModelManager(addressBook, new UserPrefs(), eventList, Set::of);
    }

    @Test
    public void execute_editedDetails_surviveStorageRoundTrip() throws Exception {
        model.addParticipation(EVENT_ID, PERSON_ID);
        model.markPresent(EVENT_ID, PERSON_ID);
        new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, Set.of(new Tag("VIP"))).execute(model);
        JsonParticipationStorage storage = new JsonParticipationStorage(testFolder.resolve("participations.json"));
        storage.saveParticipations(model.getParticipations());
        AddressBook people = new AddressBook();
        people.addPerson(PERSON);
        EventList events = new EventList();
        events.addEvent(EVENT);
        assertEquals(model.getParticipations().getParticipations(),
                storage.readParticipations(events, people).orElseThrow().getParticipations());
    }

    @Test
    public void constructor_copiesMutableSets() throws CommandException {
        Set<ParticipationRole> roles = new java.util.HashSet<>(ROLES);
        Set<Tag> tags = new java.util.HashSet<>(Set.of(new Tag("VIP")));
        EditParticipantCommand command = new EditParticipantCommand(EVENT_ID, PERSON_ID, roles, tags);
        roles.clear();
        tags.clear();
        model.addParticipation(EVENT_ID, PERSON_ID);
        command.execute(model);
        assertEquals(Set.of(new Participation(EVENT, PERSON, false, Set.of(new Tag("VIP")), ROLES)),
                model.getParticipations().getParticipations());
    }

    @Test
    public void execute_eachFieldCombination_preservesAttendanceAndOmittedFields() throws CommandException {
        Set<Tag> originalTags = Set.of(new Tag("Old"));
        for (int mask = 1; mask < 4; mask++) {
            setUp();
            model.addParticipation(EVENT_ID, PERSON_ID, ROLES, originalTags);
            model.markPresent(EVENT_ID, PERSON_ID);
            model.updateParticipantSearch(EVENT_ID, PERSON.getName().fullName);
            Set<ParticipationRole> roles = (mask & 1) != 0 ? Set.of() : null;
            Set<Tag> tags = (mask & 2) != 0 ? Set.of(new Tag("VIP")) : null;

            CommandResult result = new EditParticipantCommand(EVENT_ID, PERSON_ID, roles, tags).execute(model);

            Participation expected = new Participation(EVENT, PERSON, true,
                    tags == null ? originalTags : tags, roles == null ? ROLES : roles);
            assertEquals(Set.of(expected), model.getParticipations().getParticipations());
            assertEquals(expected, model.getParticipantSearchResults().get(0));
            assertEquals(String.format(EditParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                    result.getFeedbackToUser());
        }
    }

    @Test
    public void execute_replacesBothFields_preservesOtherPeopleAndEvents() throws CommandException {
        model.addParticipation(EVENT_ID, PERSON_ID);
        Event other = model.createEvent("Other", 10, "");
        model.addParticipation(new EventId(other.getId()), PERSON_ID);
        Person otherPerson = new PersonBuilder().withId("P2").withName("Other Guest").build();
        model.addPerson(otherPerson);
        model.addParticipation(EVENT_ID, otherPerson.getId());
        Set<Tag> tags = Set.of(new Tag("VIP"), new Tag("Guest"));

        new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, tags).execute(model);

        assertEquals(Set.of(new Participation(EVENT, PERSON, false, tags, ROLES),
                new Participation(other, PERSON), new Participation(EVENT, otherPerson)),
                model.getParticipations().getParticipations());
        new EditParticipantCommand(EVENT_ID, PERSON_ID, null, Set.of()).execute(model);
        assertEquals(Set.of(), model.getParticipationsForPerson(PERSON).stream()
                .filter(participation -> participation.getEvent().getId().equals("E1"))
                .findFirst().orElseThrow().getTags());
    }

    @Test
    public void constructorAndModel_invalidArguments_rejected() {
        assertThrows(NullPointerException.class, () -> new EditParticipantCommand(null, PERSON_ID, ROLES, null));
        assertThrows(NullPointerException.class, () -> new EditParticipantCommand(EVENT_ID, null, ROLES, null));
        assertThrows(IllegalArgumentException.class, () ->
                new EditParticipantCommand(EVENT_ID, PERSON_ID, null, null));
        assertThrows(NullPointerException.class, () ->
                new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, null).execute(null));
        assertThrows(NullPointerException.class, () -> model.editParticipation(null, PERSON_ID, ROLES, null));
        assertThrows(NullPointerException.class, () -> model.editParticipation(EVENT_ID, null, ROLES, null));
        assertThrows(IllegalArgumentException.class, () -> model.editParticipation(EVENT_ID, PERSON_ID, null, null));
    }

    @Test
    public void execute_missingParticipation_throwsCommandException() {
        EditParticipantCommand command = new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, null);

        assertCommandFailure(command, model,
                String.format(EditParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E2");
        EditParticipantCommand command = new EditParticipantCommand(missingEventId, PERSON_ID, ROLES, null);

        assertCommandFailure(command, model,
                String.format(EditParticipantCommand.MESSAGE_EVENT_NOT_FOUND, missingEventId));
    }

    @Test
    public void execute_missingPerson_throwsCommandException() {
        PersonId missingPersonId = new PersonId("P2");
        EditParticipantCommand command = new EditParticipantCommand(EVENT_ID, missingPersonId, ROLES, null);

        assertCommandFailure(command, model,
                String.format(EditParticipantCommand.MESSAGE_PERSON_NOT_FOUND, missingPersonId));
    }

    @Test
    public void equals() {
        EditParticipantCommand command = new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, null);
        EditParticipantCommand sameCommand = new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, null);
        EditParticipantCommand differentEvent = new EditParticipantCommand(
                new EventId("E2"), PERSON_ID, ROLES, null);
        EditParticipantCommand differentPerson = new EditParticipantCommand(
                EVENT_ID, new PersonId("P2"), ROLES, null);

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertEquals(command.hashCode(), sameCommand.hashCode());
        assertFalse(command.equals(new EditParticipantCommand(EVENT_ID, PERSON_ID, Set.of(), null)));
        assertFalse(command.equals(new EditParticipantCommand(EVENT_ID, PERSON_ID, ROLES, Set.of())));
        assertFalse(command.equals("other"));
        assertFalse(command.equals(differentEvent));
        assertFalse(command.equals(differentPerson));
        assertFalse(command.equals(null));
    }
}
