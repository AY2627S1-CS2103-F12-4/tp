package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventNameContainsKeywordPredicate;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddEventCommandTest {
    @Test
    public void execute_activeRoleSearch_preservesMatchingEventsAndSearchContext() {
        ModelManager model = new ModelManager();
        Event main = model.createEvent("Main", 10, "Main event");
        Event other = model.createEvent("Other", 10, "Other event");
        Person person = new PersonBuilder().withId("P1").build();
        model.addPerson(person);
        model.addParticipation(new EventId(main.getId()), person.getId(),
                Set.of(new ParticipationRole("Host")), Set.of());
        model.addParticipation(new EventId(other.getId()), person.getId(),
                Set.of(new ParticipationRole("Speaker")), Set.of());
        model.updateParticipantRoleSearch("Speaker");
        List<Participation> matches = List.copyOf(model.getParticipantSearchResults());
        assertEquals(1, matches.size());
        assertEquals(List.of(other), model.getFilteredEventList());

        CommandResult result = new AddEventCommand("Empty", 10, "No participants").execute(model);

        Event added = new Event(new EventId("E3"), "Empty", 10, "No participants");
        assertEquals(List.of(main, other, added), model.getEvents());
        assertEquals(List.of(other), model.getFilteredEventList());
        assertEquals(matches, model.getParticipantSearchResults());
        assertEquals(Optional.of("Speaker"), model.getParticipantSearchKeyword());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertFalse(model.isParticipantSearchIncludingArchived());
        assertTrue(model.getParticipationsForEvent(added).isEmpty());
        assertEquals(new CommandResult("New event added: Empty (E3)", false, false, true), result);
    }

    @Test
    public void execute_activeRoleSearchWithoutMatches_keepsEventListEmpty() {
        ModelManager model = new ModelManager();
        Event existing = model.createEvent("Main", 10, "Main event");
        model.updateParticipantRoleSearch("Speaker");
        assertTrue(model.getFilteredEventList().isEmpty());

        new AddEventCommand("Empty", 10, "No participants").execute(model);

        Event added = new Event(new EventId("E2"), "Empty", 10, "No participants");
        assertEquals(List.of(existing, added), model.getEvents());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertEquals(Optional.of("Speaker"), model.getParticipantSearchKeyword());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertFalse(model.isParticipantSearchIncludingArchived());
    }

    @Test
    public void execute_eventNameFilterWithoutParticipantSearch_showsAllNonarchivedEvents() {
        ModelManager model = new ModelManager();
        Event main = model.createEvent("Main", 10, "Main event");
        Event other = model.createEvent("Other", 10, "Other event");
        Event archived = model.createEvent("Archived", 10, "Past event").withArchived(true);
        model.updateEvent(archived);
        model.updateFilteredEventList(new EventNameContainsKeywordPredicate("Main"));
        assertEquals(List.of(main), model.getFilteredEventList());

        new AddEventCommand("Empty", 10, "No participants").execute(model);

        Event added = new Event(new EventId("E4"), "Empty", 10, "No participants");
        assertEquals(List.of(main, other, archived, added), model.getEvents());
        assertEquals(List.of(main, other, added), model.getFilteredEventList());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertTrue(model.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void equals_comparesAllEventDetails() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        AddEventCommand copy = new AddEventCommand("Orientation", 100, "Welcome session");

        assertTrue(command.equals(command));
        assertTrue(command.equals(copy));
        assertTrue(copy.equals(command));
        assertFalse(command.equals(null));
        assertFalse(command.equals("Orientation"));
        assertFalse(command.equals(new AddEventCommand("Workshop", 100, "Welcome session")));
        assertFalse(command.equals(new AddEventCommand("Orientation", 50, "Welcome session")));
        assertFalse(command.equals(new AddEventCommand("Orientation", 100, "Different description")));
    }

    @Test
    public void hashCode_equalCommands_returnsSameHashCode() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        AddEventCommand copy = new AddEventCommand("Orientation", 100, "Welcome session");

        assertEquals(command.hashCode(), copy.hashCode());
        assertEquals(command.hashCode(), command.hashCode());
    }

    @Test
    public void constructor_invalidDetails_throwsException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null, 100, "Welcome session"));
        assertThrows(NullPointerException.class, () -> new AddEventCommand("Orientation", 100, null));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand(" ", 100, "Welcome session"));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand("Orientation", 0, "Welcome session"));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand("Orientation", -1, "Welcome session"));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
