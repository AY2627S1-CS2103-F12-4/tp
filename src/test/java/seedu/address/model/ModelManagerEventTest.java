package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventNameContainsKeywordPredicate;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerEventTest {
    private static final Event WORKSHOP = new Event(new EventId("E1"), "Workshop", 10, "Learn Java");
    private static final Event CONCERT = new Event(new EventId("E2"), "Concert", 20, "Live music");
    private final ModelManager model = new ModelManager();
    private final Person alice = new PersonBuilder().withId("P1").withName("Alice Tan").build();

    @Test
    public void getFilteredEventList_addEvents_updatesExistingReadOnlyView() {
        ObservableList<Event> view = model.getFilteredEventList();
        model.updateFilteredEventList(new EventNameContainsKeywordPredicate("Workshop"));

        model.addEvent(WORKSHOP);
        model.addEvent(CONCERT);

        assertEquals(List.of(WORKSHOP), view);
        assertThrows(UnsupportedOperationException.class, () -> view.add(CONCERT));
    }

    @Test
    public void updateFilteredEventList_archiveVisibility_combinesWithPredicateAndResetsByDefault() {
        Event archived = WORKSHOP.withArchived(true);
        model.addEvent(archived);
        model.addEvent(CONCERT);
        ObservableList<Event> view = model.getFilteredEventList();
        assertEquals(List.of(CONCERT), view);

        model.updateFilteredEventList(new EventNameContainsKeywordPredicate("Workshop"), true);
        assertEquals(List.of(archived), view);
        assertThrows(NullPointerException.class, () -> model.updateFilteredEventList(null, false));
        assertEquals(List.of(archived), view);

        model.updateFilteredEventList(event -> true);
        assertEquals(List.of(CONCERT), view);
    }

    @Test
    public void showEvents_includingArchives_clearsSearchAndPreservesDataSelectionAndPersonFilter() {
        Event archived = CONCERT.withArchived(true);
        model.addEvent(WORKSHOP);
        model.addEvent(archived);
        model.addPerson(alice);
        model.addParticipation(WORKSHOP, alice);
        model.selectEvent(new EventId(WORKSHOP.getId()));
        model.updateFilteredPersonList(person -> false);
        model.updateParticipantSearch("Alice", true);
        var participations = model.getParticipations().getParticipations();

        model.showEvents(true);

        assertEquals(List.of(WORKSHOP, archived), model.getFilteredEventList());
        assertEquals(List.of(WORKSHOP, archived), model.getEvents());
        assertEquals(participations, model.getParticipations().getParticipations());
        assertEquals(alice, model.getAddressBook().getPersonList().getFirst());
        assertEquals(WORKSHOP, model.getSelectedEvent().orElseThrow());
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(), model.getParticipantSearchResults());
        assertFalse(model.getParticipantSearchEvent().isPresent());
        assertFalse(model.getParticipantSearchKeyword().isPresent());

        model.setPerson(alice, new PersonBuilder(alice).withName("Alice Lee").build());
        model.updateEvent(WORKSHOP.withArchived(true));
        assertEquals(List.of(), model.getParticipantSearchResults());
        assertEquals(List.of(WORKSHOP.withArchived(true), archived), model.getFilteredEventList());

        model.showEvents(false);
        assertEquals(List.of(), model.getFilteredEventList());
    }

    @Test
    public void clearSearch_archiveListing_restoresDefaultArchiveVisibility() {
        model.showEvents(true);
        assertEquals(List.of(), model.getFilteredEventList());
        model.addEvent(WORKSHOP.withArchived(true));
        assertEquals(List.of(WORKSHOP.withArchived(true)), model.getFilteredEventList());

        model.clearParticipantSearch();

        assertEquals(List.of(), model.getFilteredEventList());
        model.addEvent(CONCERT);
        assertEquals(List.of(CONCERT), model.getFilteredEventList());
    }

    @Test
    public void addParticipation_existingRecords_resolvesUpdatedPerson() {
        model.addEvent(WORKSHOP);
        model.addPerson(alice);
        model.addParticipation(WORKSHOP, alice);
        Person updatedAlice = new PersonBuilder(alice).withPhone("91234567").build();

        model.setPerson(alice, updatedAlice);

        Participation participation = model.getParticipationsForEvent(WORKSHOP).iterator().next();
        assertEquals(updatedAlice, participation.getPerson());
        assertEquals(WORKSHOP, participation.getEvent());
    }

    @Test
    public void addParticipation_missingRecords_rejectsParticipation() {
        assertThrows(EventNotFoundException.class, () -> model.addParticipation(WORKSHOP, alice));
        model.addEvent(WORKSHOP);
        assertThrows(PersonNotFoundException.class, () -> model.addParticipation(WORKSHOP, alice));
        assertEquals(Set.of(), model.getParticipationsForEvent(WORKSHOP));
    }

    @Test
    public void deletePerson_participant_removesParticipationBeforeIdIsReused() {
        model.addEvent(WORKSHOP);
        model.addPerson(alice);
        model.addParticipation(WORKSHOP, alice);

        model.deletePerson(alice);
        model.addPerson(alice);

        assertEquals(Set.of(), model.getParticipationsForEvent(WORKSHOP));
    }

    @Test
    public void setAddressBook_removedParticipant_doesNotRestoreOldParticipation() {
        model.addEvent(WORKSHOP);
        model.addPerson(alice);
        model.addParticipation(WORKSHOP, alice);

        model.setAddressBook(new AddressBook());
        model.addPerson(alice);

        assertEquals(Set.of(), model.getParticipationsForEvent(WORKSHOP));
        assertEquals(List.of(WORKSHOP), model.getFilteredEventList());
    }

    @Test
    public void equals_differentEventsOrParticipations_returnsFalse() {
        ModelManager other = new ModelManager();
        model.addPerson(alice);
        other.addPerson(alice);
        model.addEvent(WORKSHOP);
        assertFalse(model.equals(other));
        other.addEvent(WORKSHOP);
        assertEquals(model, other);

        model.addParticipation(WORKSHOP, alice);

        assertFalse(model.equals(other));
    }
}
