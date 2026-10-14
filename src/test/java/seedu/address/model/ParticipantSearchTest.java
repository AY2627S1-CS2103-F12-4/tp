package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ParticipantSearchTest {
    private static final EventId FIRST_EVENT_ID = new EventId("E1");
    private static final EventId SECOND_EVENT_ID = new EventId("E2");
    private static final Event FIRST_EVENT = new Event(FIRST_EVENT_ID, "Wedding", 30, "Reception");
    private static final Event SECOND_EVENT = new Event(SECOND_EVENT_ID, "Workshop", 20, "Java");
    private static final Person MICHAEL = new PersonBuilder().withId("P1").withName("Michael Tan").build();
    private static final Person MICHAELA = new PersonBuilder().withId("P2").withName("Michaela Lee").build();
    private static final Person JORDAN = new PersonBuilder().withId("P10").withName("Jordan Lee").build();

    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = createModel();
    }

    @Test
    public void constructor_noSearch_returnsEmptyContextAndResults() {
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertEquals(Optional.empty(), model.getParticipantSearchEvent());
        assertEquals(Optional.empty(), model.getParticipantSearchKeyword());
    }

    @Test
    public void updateParticipantSearch_matchesNamesAndScopesEvent() {
        model.addParticipation(SECOND_EVENT, JORDAN);
        model.updateParticipantSearch(FIRST_EVENT_ID, "  cHaE  ");
        assertEquals(List.of("P1", "P2"), resultIds());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("cHaE"), model.getParticipantSearchKeyword());
        model.updateParticipantSearch(FIRST_EVENT_ID, "Jordan");
        assertTrue(resultIds().isEmpty());
        model.updateParticipantSearch(SECOND_EVENT_ID, "Jordan");
        assertEquals(List.of("P10"), resultIds());
    }

    @Test
    public void updateParticipantSearch_repeatedSearch_replacesResultsInSameList() {
        ObservableList<Participation> results = model.getParticipantSearchResults();
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        assertEquals(2, results.size());
        model.updateParticipantSearch(FIRST_EVENT_ID, "Tan");
        assertEquals(List.of("P1"), resultIds());
        model.updateParticipantSearch(SECOND_EVENT_ID, "Missing");
        assertTrue(results.isEmpty());
        assertSame(results, model.getParticipantSearchResults());
        assertEquals(Optional.of(SECOND_EVENT), model.getParticipantSearchEvent());
    }

    @Test
    public void updateParticipantSearch_invalidInput_preservesPreviousSearch() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        List<Participation> before = List.copyOf(model.getParticipantSearchResults());
        assertThrows(NullPointerException.class, () -> model.updateParticipantSearch(null, "Michael"));
        assertThrows(NullPointerException.class, () -> model.updateParticipantSearch(FIRST_EVENT_ID, null));
        for (String keyword : List.of("", " ", "\t\n")) {
            assertThrows(IllegalArgumentException.class, () -> model.updateParticipantSearch(FIRST_EVENT_ID, keyword));
        }
        assertThrows(EventNotFoundException.class, () -> model.updateParticipantSearch(new EventId("E99"), "Tan"));
        assertEquals(before, model.getParticipantSearchResults());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Michael"), model.getParticipantSearchKeyword());
    }

    @Test
    public void getParticipantSearchResults_modification_throwsUnsupportedOperationException() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        ObservableList<Participation> results = model.getParticipantSearchResults();
        assertThrows(UnsupportedOperationException.class, () -> results.add(new Participation(FIRST_EVENT, JORDAN)));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> results.set(0, results.get(1)));
        assertThrows(UnsupportedOperationException.class, results::clear);
    }

    @Test
    public void updateParticipantSearch_sortsIdsLexicographically() {
        model.addParticipation(FIRST_EVENT, JORDAN);
        model.updateParticipantSearch(FIRST_EVENT_ID, "a");
        assertEquals(List.of("P1", "P10", "P2"), resultIds());
    }

    @Test
    public void updateParticipantSearch_notifiesObserversWithCurrentContext() {
        AtomicInteger changes = new AtomicInteger();
        model.getParticipantSearchResults().addListener((ListChangeListener<Participation>) change -> {
            changes.incrementAndGet();
            assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
            assertEquals(Optional.of("Michael"), model.getParticipantSearchKeyword());
        });
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        assertTrue(changes.get() > 0);
    }

    @Test
    public void updateFilters_activeSearch_preservesSelectedEventAndResults() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.updateFilteredEventList(event -> event.equals(SECOND_EVENT));
        model.updateFilteredPersonList(person -> false);
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(List.of("P1", "P2"), resultIds());
        model.removeParticipation(FIRST_EVENT, MICHAEL);
        assertEquals(List.of("P2"), resultIds());
        assertEquals(List.of(SECOND_EVENT), model.getFilteredEventList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void participationChanges_bothOverloads_refreshResults() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.removeParticipation(FIRST_EVENT, MICHAEL);
        assertEquals(List.of("P2"), resultIds());
        model.removeParticipation(FIRST_EVENT_ID, MICHAELA.getId());
        assertTrue(resultIds().isEmpty());
        model.addParticipation(FIRST_EVENT_ID, MICHAEL.getId());
        assertEquals(List.of("P1"), resultIds());
        model.addParticipation(FIRST_EVENT, MICHAELA);
        assertEquals(List.of("P1", "P2"), resultIds());
    }

    @Test
    public void participationChanges_otherEvent_doNotLeakIntoResults() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.addParticipation(SECOND_EVENT, MICHAEL);
        model.removeParticipation(SECOND_EVENT, MICHAEL);
        assertEquals(List.of("P1", "P2"), resultIds());
    }

    @Test
    public void setPerson_nameChanges_enterAndLeaveResults() {
        model.addParticipation(FIRST_EVENT, JORDAN);
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        Person renamedMichael = new PersonBuilder(MICHAEL).withName("Alex Tan").build();
        model.setPerson(MICHAEL, renamedMichael);
        assertEquals(List.of("P2"), resultIds());
        Person renamedJordan = new PersonBuilder(JORDAN).withName("Michael Jordan").build();
        model.setPerson(JORDAN, renamedJordan);
        assertEquals(List.of("P10", "P2"), resultIds());
        assertEquals("Michael Jordan", model.getParticipantSearchResults().getFirst().getPerson().getName().fullName);
    }

    @Test
    public void setPerson_contactChange_updatesRowsAndNotifiesObservers() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Tan");
        AtomicInteger changes = new AtomicInteger();
        model.getParticipantSearchResults().addListener((ListChangeListener<Participation>) change ->
                changes.incrementAndGet());
        Person updated = new PersonBuilder(MICHAEL).withPhone("81234567").build();
        model.setPerson(MICHAEL, updated);
        assertEquals("81234567", model.getParticipantSearchResults().getFirst().getPerson().getPhone().value);
        assertTrue(changes.get() > 0);
    }

    @Test
    public void deletePerson_matchingPerson_removesResultAndMembership() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.deletePerson(MICHAEL);
        assertEquals(List.of("P2"), resultIds());
        assertEquals(1, model.getParticipationsForEvent(FIRST_EVENT).size());
    }

    @Test
    public void addPerson_matchingNameWithoutMembership_doesNotAddResult() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        Person newPerson = new PersonBuilder().withId("P3").withName("Michael Smith").build();
        model.addPerson(newPerson);
        assertEquals(List.of("P1", "P2"), resultIds());
        model.addParticipation(FIRST_EVENT, newPerson);
        assertEquals(List.of("P1", "P2", "P3"), resultIds());
    }

    @Test
    public void setAddressBook_replacedPeople_refreshesResultsAndClearsRemovedMemberships() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        AddressBook replacement = new AddressBook();
        replacement.addPerson(new PersonBuilder(MICHAEL).withName("Michael Smith").build());
        model.setAddressBook(replacement);
        assertEquals(List.of("P1"), resultIds());
        assertEquals("Michael Smith", model.getParticipantSearchResults().getFirst().getPerson().getName().fullName);
        model.setAddressBook(new AddressBook());
        assertTrue(resultIds().isEmpty());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Michael"), model.getParticipantSearchKeyword());
    }

    @Test
    public void attendanceAndTags_updatedState_visibleInResults() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Tan");
        model.markPresent(FIRST_EVENT_ID, MICHAEL.getId());
        assertTrue(model.getParticipantSearchResults().getFirst().isPresent());
        Participation absent = model.setPresent(FIRST_EVENT, MICHAEL, false);
        assertFalse(model.getParticipantSearchResults().getFirst().isPresent());
        assertEquals(absent, model.getParticipantSearchResults().getFirst());
        Participation tagged = model.setTags(FIRST_EVENT, MICHAEL, Set.of(new Tag("vip")));
        assertEquals(Set.of(new Tag("vip")), model.getParticipantSearchResults().getFirst().getTags());
        assertEquals(tagged, model.getParticipantSearchResults().getFirst());
    }

    @Test
    public void failedMutations_preserveSearchResults() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        List<Participation> before = List.copyOf(model.getParticipantSearchResults());
        assertThrows(DuplicateParticipationException.class, () -> model.addParticipation(FIRST_EVENT, MICHAEL));
        Person missing = new PersonBuilder().withId("P99").build();
        assertThrows(PersonNotFoundException.class, () -> model.addParticipation(FIRST_EVENT_ID, missing.getId()));
        assertEquals(before, model.getParticipantSearchResults());
    }

    @Test
    public void updateParticipantSearch_preservesStoredData() {
        AddressBook people = new AddressBook(model.getAddressBook());
        List<Event> events = model.getEvents();
        Set<Participation> participations = model.getParticipations().getParticipations();
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.updateParticipantSearch(SECOND_EVENT_ID, "Missing");
        assertEquals(people, model.getAddressBook());
        assertEquals(events, model.getEvents());
        assertEquals(participations, model.getParticipations().getParticipations());
    }

    @Test
    public void equals_searchContextIncluded() {
        ModelManager other = createModel();
        model.updateParticipantSearch(FIRST_EVENT_ID, "Missing");
        assertNotEquals(model, other);
        other.updateParticipantSearch(SECOND_EVENT_ID, "Missing");
        assertNotEquals(model, other);
        other.updateParticipantSearch(FIRST_EVENT_ID, "Different");
        assertNotEquals(model, other);
        other.updateParticipantSearch(FIRST_EVENT_ID, "Missing");
        assertEquals(model, other);
    }

    @Test
    public void deleteEvent_selectedSearchEvent_clearsContextAndAllowsLaterMutations() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        ObservableList<Participation> results = model.getParticipantSearchResults();
        model.deleteEvent(FIRST_EVENT_ID);
        assertTrue(results.isEmpty());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        model.setPerson(MICHAEL, new PersonBuilder(MICHAEL).withName("Michael Smith").build());
        assertTrue(results.isEmpty());
        model.updateParticipantSearch(SECOND_EVENT_ID, "Michael");
        assertEquals(Optional.of(SECOND_EVENT), model.getParticipantSearchEvent());
    }

    @Test
    public void deleteEvent_otherOrMissingEvent_preservesSearch() {
        model.updateParticipantSearch(FIRST_EVENT_ID, "Michael");
        model.deleteEvent(SECOND_EVENT_ID);
        assertEquals(List.of("P1", "P2"), resultIds());
        assertThrows(EventNotFoundException.class, () -> model.deleteEvent(new EventId("E99")));
        assertEquals(List.of("P1", "P2"), resultIds());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Michael"), model.getParticipantSearchKeyword());
    }

    private List<String> resultIds() {
        return model.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList();
    }

    private ModelManager createModel() {
        ModelManager result = new ModelManager();
        result.addEvent(FIRST_EVENT);
        result.addEvent(SECOND_EVENT);
        result.addPerson(MICHAEL);
        result.addPerson(MICHAELA);
        result.addPerson(JORDAN);
        result.addParticipation(FIRST_EVENT, MICHAELA);
        result.addParticipation(FIRST_EVENT, MICHAEL);
        return result;
    }
}
