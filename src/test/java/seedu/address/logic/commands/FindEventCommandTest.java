package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventNameContainsKeywordPredicate;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the interaction between {@code FindEventCommand} and the event list in the model.
 * Defines the event filtering API to be implemented in {@code Model} and {@code ModelManager}.
 */
public class FindEventCommandTest {
    private static final Event WORKSHOP = new Event(new EventId("E1"), "Java Workshop", 10, "Learn Java");
    private static final Event CONCERT = new Event(new EventId("E2"), "Concert", 20, "Live music");
    private static final Event MEETUP = new Event(new EventId("E3"), "Java Meetup", 30, "Meet developers");

    @Test
    public void execute_matchingEvents_listsMatchesAndCount() {
        Model model = createModelWithEvents();

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);

        assertEquals(List.of(WORKSHOP, MEETUP), model.getFilteredEventList());
        assertEquals(new CommandResult("2 event(s) listed!", false, false, true), result);
    }

    @Test
    public void execute_noMatches_clearsPreviousResultsAndReportsNoMatches() {
        Model model = createModelWithEvents();
        new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);
        assertEquals(List.of(WORKSHOP, MEETUP), model.getFilteredEventList());

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Football")).execute(model);

        assertEquals(List.of(), model.getFilteredEventList());
        assertEquals(new CommandResult("No matching events found.", false, false, true), result);
    }

    @Test
    public void execute_emptyEventList_reportsNoMatches() {
        Model model = new ModelManager();

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);

        assertEquals(List.of(), model.getFilteredEventList());
        assertEquals(new CommandResult("No matching events found.", false, false, true), result);
    }

    @Test
    public void execute_secondSearch_replacesPreviousFilter() {
        Model model = createModelWithEvents();
        new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);
        assertEquals(List.of(WORKSHOP, MEETUP), model.getFilteredEventList());

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Concert")).execute(model);

        assertEquals(List.of(CONCERT), model.getFilteredEventList());
        assertEquals(new CommandResult("1 event(s) listed!", false, false, true), result);
    }

    @Test
    public void execute_search_preservesStoredEvents() {
        Model model = createModelWithEvents();

        new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);
        assertEquals(List.of(WORKSHOP, MEETUP), model.getFilteredEventList());

        // Removing the filter must restore every original event, including nonmatches.
        model.updateFilteredEventList(event -> true);
        assertEquals(List.of(WORKSHOP, CONCERT, MEETUP), model.getFilteredEventList());
    }

    @Test
    public void execute_multipleParticipants_listsEachEventOnceAndPreservesParticipations() {
        Model model = createModelWithEvents();
        Person alice = new PersonBuilder().withId("P1").withName("Alice Tan").build();
        Person bob = new PersonBuilder().withId("P2").withName("Bob Lim").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addParticipation(WORKSHOP, alice);
        model.addParticipation(WORKSHOP, bob);
        Set<Participation> originalParticipations = model.getParticipationsForEvent(WORKSHOP);
        assertEquals(Set.of(
                new Participation(WORKSHOP, alice),
                new Participation(WORKSHOP, bob)), originalParticipations);

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);

        // The meetup has no participants and must still be found.
        assertEquals(List.of(WORKSHOP, MEETUP), model.getFilteredEventList());
        assertEquals(new CommandResult("2 event(s) listed!", false, false, true), result);
        assertEquals(originalParticipations, model.getParticipationsForEvent(WORKSHOP));
        assertEquals(Set.of(), model.getParticipationsForEvent(MEETUP));
    }

    private Model createModelWithEvents() {
        Model model = new ModelManager();
        model.addEvent(WORKSHOP);
        model.addEvent(CONCERT);
        model.addEvent(MEETUP);
        return model;
    }
}
