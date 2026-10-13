package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventNameContainsKeywordPredicate;
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
    }
}
