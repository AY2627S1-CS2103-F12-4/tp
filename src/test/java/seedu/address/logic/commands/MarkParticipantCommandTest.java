package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

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
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.testutil.PersonBuilder;

public class MarkParticipantCommandTest {

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
    public void execute_existingParticipation_marksParticipantPresent() throws CommandException {
        model.addParticipation(EVENT_ID, PERSON_ID);
        MarkParticipantCommand command = new MarkParticipantCommand(EVENT_ID, PERSON_ID);

        CommandResult result = command.execute(model);

        Participation participation = model.getParticipations().getParticipations().iterator().next();
        assertTrue(participation.isPresent());
        assertEquals(String.format(MarkParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_alreadyPresent_throwsCommandException() {
        model.addParticipation(EVENT_ID, PERSON_ID);
        model.setPresent(EVENT, PERSON, true);
        MarkParticipantCommand command = new MarkParticipantCommand(EVENT_ID, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(MarkParticipantCommand.MESSAGE_ALREADY_PRESENT, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingParticipation_throwsCommandException() {
        MarkParticipantCommand command = new MarkParticipantCommand(EVENT_ID, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(MarkParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E2");
        MarkParticipantCommand command = new MarkParticipantCommand(missingEventId, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(MarkParticipantCommand.MESSAGE_EVENT_NOT_FOUND, missingEventId));
    }

    @Test
    public void execute_missingPerson_throwsCommandException() {
        PersonId missingPersonId = new PersonId("P2");
        MarkParticipantCommand command = new MarkParticipantCommand(EVENT_ID, missingPersonId);

        assertCommandFailure(command, model,
                String.format(MarkParticipantCommand.MESSAGE_PERSON_NOT_FOUND, missingPersonId));
    }

    @Test
    public void equals() {
        MarkParticipantCommand command = new MarkParticipantCommand(EVENT_ID, PERSON_ID);
        MarkParticipantCommand sameCommand = new MarkParticipantCommand(EVENT_ID, PERSON_ID);
        MarkParticipantCommand differentEvent = new MarkParticipantCommand(new EventId("E2"), PERSON_ID);
        MarkParticipantCommand differentPerson = new MarkParticipantCommand(EVENT_ID, new PersonId("P2"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertFalse(command.equals(differentEvent));
        assertFalse(command.equals(differentPerson));
        assertFalse(command.equals(null));
    }
}
