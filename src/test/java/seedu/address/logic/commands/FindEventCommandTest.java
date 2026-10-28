package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Locale;
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
    public void execute_excludeArchived_listsOnlyActiveMatches() {
        Model model = createModelWithEvents();
        model.updateEvent(WORKSHOP.withArchived(true));
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("Java");

        for (FindEventCommand command : List.of(new FindEventCommand(predicate),
                new FindEventCommand(predicate, false))) {
            model.showEvents(true);

            CommandResult result = command.execute(model);

            assertEquals(List.of(MEETUP), model.getFilteredEventList());
            assertEquals(new CommandResult("1 event(s) listed!", false, false, true), result);
        }
    }

    @Test
    public void execute_includeArchived_listsOnlyMatchingActiveAndArchivedEvents() {
        Model model = createModelWithEvents();
        Event archivedWorkshop = WORKSHOP.withArchived(true);
        Event archivedConcert = CONCERT.withArchived(true);
        model.updateEvent(archivedWorkshop);
        model.updateEvent(archivedConcert);
        Person alice = new PersonBuilder().withId("P1").build();
        model.addPerson(alice);
        model.addParticipation(archivedWorkshop, alice);
        Set<Participation> originalParticipations = model.getParticipationsForEvent(archivedWorkshop);

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("java"), true)
                .execute(model);

        assertEquals(List.of(archivedWorkshop, MEETUP), model.getFilteredEventList());
        assertEquals(new CommandResult("2 event(s) listed!", false, false, true), result);
        assertEquals(List.of(archivedWorkshop, archivedConcert, MEETUP), model.getEvents());
        assertEquals(originalParticipations, model.getParticipationsForEvent(archivedWorkshop));
    }

    @Test
    public void execute_includeArchivedWithoutMatches_clearsPreviousResultsAndReportsNoMatches() {
        Model model = createModelWithEvents();
        model.updateEvent(WORKSHOP.withArchived(true));
        new FindEventCommand(new EventNameContainsKeywordPredicate("Java"), true).execute(model);

        CommandResult result = new FindEventCommand(new EventNameContainsKeywordPredicate("Football"), true)
                .execute(model);

        assertEquals(List.of(), model.getFilteredEventList());
        assertEquals(new CommandResult("No matching events found.", false, false, true), result);
    }

    @Test
    public void execute_successiveSearches_replaceKeywordAndArchiveScope() {
        Model model = createModelWithEvents();
        Event archivedWorkshop = WORKSHOP.withArchived(true);
        Event archivedConcert = CONCERT.withArchived(true);
        model.updateEvent(archivedWorkshop);
        model.updateEvent(archivedConcert);

        new FindEventCommand(new EventNameContainsKeywordPredicate("Java"), true).execute(model);
        assertEquals(List.of(archivedWorkshop, MEETUP), model.getFilteredEventList());

        new FindEventCommand(new EventNameContainsKeywordPredicate("Java")).execute(model);
        assertEquals(List.of(MEETUP), model.getFilteredEventList());

        new FindEventCommand(new EventNameContainsKeywordPredicate("Concert"), true).execute(model);
        assertEquals(List.of(archivedConcert), model.getFilteredEventList());
    }

    @Test
    public void equalsAndHashCode_comparePredicateAndArchiveScope() {
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("Java");
        FindEventCommand command = new FindEventCommand(predicate, true);
        FindEventCommand sameCommand = new FindEventCommand(new EventNameContainsKeywordPredicate("Java"), true);

        assertEquals(command, command);
        assertEquals(command, sameCommand);
        assertEquals(command.hashCode(), sameCommand.hashCode());
        assertEquals(new FindEventCommand(predicate), new FindEventCommand(predicate, false));
        assertEquals(new FindEventCommand(predicate).hashCode(), new FindEventCommand(predicate, false).hashCode());
        assertNotEquals(command, new FindEventCommand(predicate, false));
        assertNotEquals(command, new FindEventCommand(new EventNameContainsKeywordPredicate("Concert"), true));
        assertNotEquals(command, null);
        assertNotEquals(command, "Java");
    }

    @Test
    public void constructor_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindEventCommand(null));
        assertThrows(NullPointerException.class, () -> new FindEventCommand(null, true));
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

    @Test
    public void execute_turkishDefaultLocale_listsMatchesWithinArchiveScope() {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Model model = new ModelManager();
            Event dinner = new Event(new EventId("E1"), "DINNER", 10, "Locale reproduction");
            Event archivedDinner = new Event(new EventId("E3"), "DINNER RECEPTION", 20, "Reception")
                    .withArchived(true);
            model.addEvent(dinner);
            model.addEvent(archivedDinner);
            model.addEvent(CONCERT);

            for (String keyword : List.of("i", "I", "dinner", "DINNER")) {
                EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate(keyword);

                CommandResult activeResult = new FindEventCommand(predicate).execute(model);
                assertEquals(List.of(dinner), model.getFilteredEventList(), keyword);
                assertEquals(new CommandResult("1 event(s) listed!", false, false, true), activeResult);

                CommandResult archivedResult = new FindEventCommand(predicate, true).execute(model);
                assertEquals(List.of(dinner, archivedDinner), model.getFilteredEventList(), keyword);
                assertEquals(new CommandResult("2 event(s) listed!", false, false, true), archivedResult);
            }
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    private Model createModelWithEvents() {
        Model model = new ModelManager();
        model.addEvent(WORKSHOP);
        model.addEvent(CONCERT);
        model.addEvent(MEETUP);
        return model;
    }
}
