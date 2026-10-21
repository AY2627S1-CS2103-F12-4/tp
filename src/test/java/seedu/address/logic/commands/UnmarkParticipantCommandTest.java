package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import seedu.address.testutil.PersonBuilder;

public class UnmarkParticipantCommandTest {

    private static final EventId EVENT_ID = new EventId("E1");
    private static final PersonId PERSON_ID = new PersonId("P1");
    private static final Event EVENT = new Event(EVENT_ID, "Workshop", 10, "Learn Java");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();

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
    public void constructor_nullIds_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnmarkParticipantCommand(null, PERSON_ID));
        assertThrows(NullPointerException.class, () -> new UnmarkParticipantCommand(EVENT_ID, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new UnmarkParticipantCommand(EVENT_ID, PERSON_ID).execute(null));
    }

    @Test
    public void execute_presentParticipant_preservesDetailsAndOtherAttendance() throws CommandException {
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Speaker"));
        Set<Tag> tags = Set.of(new Tag("VIP"));
        model.addParticipation(EVENT_ID, PERSON_ID, roles, tags);
        model.markPresent(EVENT_ID, PERSON_ID);

        Event otherEvent = new Event(new EventId("E2"), "Dinner", 20, "Team dinner");
        model.addEvent(otherEvent);
        model.addParticipation(new EventId("E2"), PERSON_ID);
        model.markPresent(new EventId("E2"), PERSON_ID);

        Person otherPerson = new PersonBuilder().withId("P2").withName("Other Guest").build();
        model.addPerson(otherPerson);
        model.addParticipation(EVENT_ID, otherPerson.getId());
        model.markPresent(EVENT_ID, otherPerson.getId());

        new UnmarkParticipantCommand(EVENT_ID, PERSON_ID).execute(model);

        assertEquals(Set.of(new Participation(EVENT, PERSON, false, tags, roles),
                new Participation(EVENT, otherPerson, true, Set.of(), Set.of()),
                new Participation(otherEvent, PERSON, true, Set.of(), Set.of())),
                model.getParticipations().getParticipations());
    }

    @Test
    public void execute_activeSearch_refreshesAttendanceAndPreservesContext() throws CommandException {
        model.addParticipation(EVENT_ID, PERSON_ID);
        model.markPresent(EVENT_ID, PERSON_ID);
        String keyword = PERSON.getName().fullName;
        model.updateParticipantSearch(EVENT_ID, keyword);
        assertTrue(model.getParticipantSearchResults().get(0).isPresent());

        new UnmarkParticipantCommand(EVENT_ID, PERSON_ID).execute(model);

        assertEquals(1, model.getParticipantSearchResults().size());
        assertFalse(model.getParticipantSearchResults().get(0).isPresent());
        assertEquals(EVENT, model.getParticipantSearchEvent().orElseThrow());
        assertEquals(keyword, model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void execute_unmarkThenMark_canCheckInAgain() throws CommandException {
        model.addParticipation(EVENT_ID, PERSON_ID);
        model.markPresent(EVENT_ID, PERSON_ID);
        new UnmarkParticipantCommand(EVENT_ID, PERSON_ID).execute(model);

        new MarkParticipantCommand(EVENT_ID, PERSON_ID).execute(model);

        assertTrue(model.getParticipationsForEvent(EVENT).iterator().next().isPresent());
    }

    @Test
    public void execute_existingParticipation_marksParticipantAbsent() throws CommandException {
        model.addParticipation(EVENT_ID, PERSON_ID);
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(EVENT_ID, PERSON_ID);

        model.setPresent(EVENT, PERSON, true);
        CommandResult result = command.execute(model);

        Participation participation = model.getParticipations().getParticipations().iterator().next();
        assertFalse(participation.isPresent());
        assertEquals(String.format(UnmarkParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_alreadyAbsent_throwsCommandException() {
        model.addParticipation(EVENT_ID, PERSON_ID);
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(EVENT_ID, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(UnmarkParticipantCommand.MESSAGE_ALREADY_ABSENT, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingParticipation_throwsCommandException() {
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(EVENT_ID, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(UnmarkParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E2");
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(missingEventId, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(UnmarkParticipantCommand.MESSAGE_EVENT_NOT_FOUND, missingEventId));
    }

    @Test
    public void execute_missingPerson_throwsCommandException() {
        PersonId missingPersonId = new PersonId("P2");
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(EVENT_ID, missingPersonId);

        assertCommandFailure(command, model,
                String.format(UnmarkParticipantCommand.MESSAGE_PERSON_NOT_FOUND, missingPersonId));
    }

    @Test
    public void equals() {
        UnmarkParticipantCommand command = new UnmarkParticipantCommand(EVENT_ID, PERSON_ID);
        UnmarkParticipantCommand sameCommand = new UnmarkParticipantCommand(EVENT_ID, PERSON_ID);
        UnmarkParticipantCommand differentEvent = new UnmarkParticipantCommand(new EventId("E2"), PERSON_ID);
        UnmarkParticipantCommand differentPerson = new UnmarkParticipantCommand(EVENT_ID, new PersonId("P2"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertFalse(command.equals(differentEvent));
        assertFalse(command.equals(differentPerson));
        assertFalse(command.equals(null));
    }
}
