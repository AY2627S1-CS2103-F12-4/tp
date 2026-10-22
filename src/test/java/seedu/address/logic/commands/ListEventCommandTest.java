package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventNameContainsKeywordPredicate;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ListEventCommandTest {
    @Test
    public void execute_filteredEvents_restoresAllEventsInOrderWithoutChangingOtherData() {
        ModelManager model = new ModelManager();
        Event workshop = model.createEvent("Workshop", 10, "Java");
        Event concert = model.createEvent("Concert", 20, "Music");
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.addParticipation(workshop, person);
        var participations = model.getParticipationsForEvent(workshop);
        model.updateFilteredPersonList(unused -> false);
        model.updateFilteredEventList(new EventNameContainsKeywordPredicate("Workshop"));
        assertEquals(List.of(workshop), model.getFilteredEventList());

        CommandResult result = new ListEventCommand().execute(model);

        assertEquals(new CommandResult("2 event(s) listed!", false, false, true), result);
        assertEquals(List.of(workshop, concert), model.getFilteredEventList());
        assertEquals(List.of(workshop, concert), model.getEvents());
        assertEquals(participations, model.getParticipationsForEvent(workshop));
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyList_reportsNoEventsAndShowsEventPanel() {
        ModelManager model = new ModelManager();
        assertEquals(new CommandResult(ListEventCommand.MESSAGE_NO_EVENTS, false, false, true),
                new ListEventCommand().execute(model));
        assertEquals(List.of(), model.getFilteredEventList());
    }

    @Test
    public void execute_noMatchesAndRepeatedCalls_restoresEventsAndReturnsSameResult() {
        ModelManager model = new ModelManager();
        Event event = model.createEvent("Workshop", 10, "");
        model.updateFilteredEventList(unused -> false);
        ListEventCommand command = new ListEventCommand();
        CommandResult first = command.execute(model);
        assertEquals(new CommandResult("1 event(s) listed!", false, false, true), first);
        assertEquals(first, command.execute(model));
        assertEquals(List.of(event), model.getFilteredEventList());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListEventCommand().execute(null));
        assertThrows(NullPointerException.class, () -> new ListEventCommand(true).execute(null));
    }

    @Test
    public void parseAndExecute_archiveListing_clearsEitherSearchAndDefaultListingHidesArchives() throws Exception {
        ModelManager model = new ModelManager();
        Event archived = model.createEvent("Workshop", 10, "").withArchived(true);
        model.updateEvent(archived);
        Event active = model.createEvent("Concert", 20, "");
        Person person = new PersonBuilder().withName("Alice Tan").build();
        model.addPerson(person);
        model.addParticipation(new EventId(archived.getId()), person.getId(),
                Set.of(new ParticipationRole("Speaker")), Set.of());
        model.updateFilteredPersonList(unused -> false);
        Set<Participation> participations = model.getParticipations().getParticipations();
        AddressBookParser parser = new AddressBookParser();

        for (String search : List.of("search-participant Alice /include-archive",
                "search-role Speaker /event " + archived.getId() + " /include-archive")) {
            parser.parseCommand(search).execute(model);
            assertEquals(1, model.getParticipantSearchResults().size());

            CommandResult result = parser.parseCommand("list-event /include-archive").execute(model);

            assertEquals(new CommandResult("2 event(s) listed!", false, false, true), result);
            assertEquals(List.of(archived, active), model.getFilteredEventList());
            assertTrue(model.getParticipantSearchKeyword().isEmpty());
            assertTrue(model.getParticipantSearchEvent().isEmpty());
            assertTrue(model.getParticipantSearchResults().isEmpty());
            assertEquals(participations, model.getParticipations().getParticipations());
            assertTrue(model.getFilteredPersonList().isEmpty());

            CommandResult defaultResult = parser.parseCommand("list-event").execute(model);
            assertEquals(new CommandResult("1 event(s) listed!", false, false, true), defaultResult);
            assertEquals(List.of(active), model.getFilteredEventList());
        }
    }

    @Test
    public void execute_emptyAndArchivedOnlyLists_returnsCorrectCounts() {
        ModelManager model = new ModelManager();
        assertEquals(new CommandResult(ListEventCommand.MESSAGE_NO_EVENTS, false, false, true),
                new ListEventCommand(true).execute(model));
        Event archived = model.createEvent("Workshop", 10, "").withArchived(true);
        model.updateEvent(archived);
        assertEquals(new CommandResult(ListEventCommand.MESSAGE_NO_EVENTS, false, false, true),
                new ListEventCommand().execute(model));
        assertEquals(new CommandResult("1 event(s) listed!", false, false, true),
                new ListEventCommand(true).execute(model));
        assertEquals(List.of(archived), model.getFilteredEventList());
    }

    @Test
    public void parseAndExecute_invalidArchiveListing_preservesSearchResults() throws Exception {
        ModelManager model = new ModelManager();
        Event archived = model.createEvent("Workshop", 10, "").withArchived(true);
        model.updateEvent(archived);
        Person person = new PersonBuilder().withName("Alice Tan").build();
        model.addPerson(person);
        model.addParticipation(archived, person);
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("search-participant Alice /include-archive").execute(model);
        List<Participation> results = List.copyOf(model.getParticipantSearchResults());

        assertThrows(ParseException.class, () ->
                parser.parseCommand("list-event /include-archive true").execute(model));

        assertEquals(results, model.getParticipantSearchResults());
        assertEquals(List.of(archived), model.getFilteredEventList());
        assertEquals("Alice", model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void equalsAndHashCode_archiveVisibility_distinguishesCommands() {
        ListEventCommand command = new ListEventCommand(true);
        ListEventCommand copy = new ListEventCommand(true);
        assertEquals(command, command);
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertNotEquals(command, new ListEventCommand());
        assertNotEquals(command, null);
        assertNotEquals(command, "list-event");
        assertEquals(new ListEventCommand(), new ListEventCommand(false));
        assertEquals(new ListEventCommand().hashCode(), new ListEventCommand(false).hashCode());
    }
}
