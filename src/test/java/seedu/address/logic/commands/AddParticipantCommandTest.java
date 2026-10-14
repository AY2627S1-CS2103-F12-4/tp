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
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddParticipantCommandTest {

    private static final EventId EVENT_ID = new EventId("E1");
    private static final PersonId PERSON_ID = new PersonId("P1");
    private static final Event EVENT = new Event(EVENT_ID, "Workshop", 10, "Learn Java");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();
    private static final Set<ParticipationRole> ROLES = Set.of(new ParticipationRole("Facilitator"));
    private static final Set<Tag> TAGS = Set.of(new Tag("VIP"));

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
    public void execute_existingEventAndPerson_addsParticipant() throws CommandException {
        AddParticipantCommand command = new AddParticipantCommand(EVENT_ID, PERSON_ID, ROLES, TAGS);

        CommandResult result = command.execute(model);

        assertEquals(String.format(AddParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
        assertEquals(1, model.getParticipations().getParticipations().size());
        Participation participation = model.getParticipations().getParticipations().iterator().next();
        assertEquals(ROLES, participation.getRoles());
        assertEquals(TAGS, participation.getTags());
    }

    @Test
    public void execute_duplicateParticipant_throwsCommandException() throws CommandException {
        AddParticipantCommand command = new AddParticipantCommand(EVENT_ID, PERSON_ID);
        command.execute(model);

        assertCommandFailure(command, model,
                String.format(AddParticipantCommand.MESSAGE_DUPLICATE_PARTICIPANT, EVENT_ID, PERSON_ID));
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E2");
        AddParticipantCommand command = new AddParticipantCommand(missingEventId, PERSON_ID);

        assertCommandFailure(command, model,
                String.format(AddParticipantCommand.MESSAGE_EVENT_NOT_FOUND, missingEventId));
    }

    @Test
    public void execute_missingPerson_throwsCommandException() {
        PersonId missingPersonId = new PersonId("P2");
        AddParticipantCommand command = new AddParticipantCommand(EVENT_ID, missingPersonId);

        assertCommandFailure(command, model,
                String.format(AddParticipantCommand.MESSAGE_PERSON_NOT_FOUND, missingPersonId));
    }

    @Test
    public void equals() {
        AddParticipantCommand command = new AddParticipantCommand(EVENT_ID, PERSON_ID);
        AddParticipantCommand sameCommand = new AddParticipantCommand(EVENT_ID, PERSON_ID);
        AddParticipantCommand differentEvent = new AddParticipantCommand(new EventId("E2"), PERSON_ID);
        AddParticipantCommand differentPerson = new AddParticipantCommand(EVENT_ID, new PersonId("P2"));
        AddParticipantCommand differentRoles = new AddParticipantCommand(EVENT_ID, PERSON_ID,
                Set.of(new ParticipationRole("Speaker")), Set.of());
        AddParticipantCommand differentTags = new AddParticipantCommand(EVENT_ID, PERSON_ID,
                Set.of(), Set.of(new Tag("Alumni")));

        assertTrue(command.equals(command));
        assertTrue(command.equals(sameCommand));
        assertFalse(command.equals(differentEvent));
        assertFalse(command.equals(differentPerson));
        assertFalse(command.equals(differentRoles));
        assertFalse(command.equals(differentTags));
        assertFalse(command.equals(null));
    }
}
