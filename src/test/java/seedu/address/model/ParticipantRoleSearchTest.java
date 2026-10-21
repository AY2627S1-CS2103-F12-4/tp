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
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ParticipantRoleSearchTest {
    // Insert E2 before E1 to distinguish insertion order from ID order.
    private static final EventId FIRST_ID = new EventId("E2");
    private static final EventId SECOND_ID = new EventId("E1");
    private static final Event FIRST_EVENT = new Event(FIRST_ID, "Workshop", 20, "");
    private static final Event SECOND_EVENT = new Event(SECOND_ID, "Conference", 20, "");
    private static final Event ARCHIVED_EVENT = new Event(new EventId("E3"), "Archived", 20, "", true);
    private static final Event EMPTY_EVENT = new Event(new EventId("E4"), "Empty", 20, "");
    private static final Person ALICE = new PersonBuilder().withId("P2").withName("Alice Tan").build();
    private static final Person BOB = new PersonBuilder().withId("P10").withName("Bob Lee").build();
    private static final Person CHARLIE = new PersonBuilder().withId("P1").withName("Speaker").build();
    private static final Person DANA = new PersonBuilder().withId("P20").withName("Dana Lim").build();
    private static final Set<ParticipationRole> ALICE_ROLES = Set.of(new ParticipationRole("Speaker"),
            new ParticipationRole("Assistant Speaker"), new ParticipationRole("Host"));

    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = createModel();
    }

    @Test
    public void globalSearch_filtersEventsAndOrdersRegistrationsWithoutDuplicates() {
        model.updateParticipantRoleSearch("  sPeAk  ");
        assertEquals(List.of(FIRST_EVENT, SECOND_EVENT), model.getFilteredEventList());
        assertEquals(List.of("E2/P10", "E2/P2", "E1/P2"), resultIds());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertEquals(Optional.of("sPeAk"), model.getParticipantSearchKeyword());
        assertEquals(ALICE_ROLES, model.getParticipantSearchResults().get(1).getRoles());
        assertTrue(model.getParticipantSearchResultsForEvent(ARCHIVED_EVENT).isEmpty());
    }

    @Test
    public void scopedSearch_resolvesHiddenEventAndNormalizesQuery() {
        model.updateFilteredEventList(event -> false);
        model.updateFilteredPersonList(person -> false);
        model.selectEvent(SECOND_ID);

        model.updateParticipantRoleSearch(FIRST_ID, " \u2003Keynote\t Speaker\u2003 ");

        assertEquals(List.of("E2/P10"), resultIds());
        assertEquals(List.of(FIRST_EVENT), model.getFilteredEventList());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Keynote Speaker"), model.getParticipantSearchKeyword());
        assertEquals(Optional.of(SECOND_EVENT), model.getSelectedEvent());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void scopedSearch_excludesRolesInOtherEventsAndArchivedMemberships() {
        model.updateParticipantRoleSearch(SECOND_ID, "Guest");
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
        model.updateParticipantRoleSearch(new EventId(ARCHIVED_EVENT.getId()), "Speaker");
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertEquals(Optional.of(ARCHIVED_EVENT), model.getParticipantSearchEvent());
    }

    @Test
    public void roleSearch_ignoresNamesAndRegistrationsWithoutRoles() {
        model.updateParticipantRoleSearch("Speaker");
        assertFalse(model.getParticipantSearchResults().stream()
                .anyMatch(participation -> participation.getPerson().getId().equals(CHARLIE.getId())));
        model.updateParticipantRoleSearch("Dana");
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void repeatedSearches_replaceResultsInSameUnmodifiableView() {
        ObservableList<Participation> results = model.getParticipantSearchResults();
        model.updateParticipantRoleSearch("Speaker");
        assertEquals(3, results.size());
        model.updateParticipantRoleSearch(FIRST_ID, "Guest");
        assertEquals(List.of("E2/P1"), resultIds());
        model.updateParticipantRoleSearch("Missing");
        assertTrue(results.isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertSame(results, model.getParticipantSearchResults());
        assertThrows(UnsupportedOperationException.class, () -> results.add(new Participation(FIRST_EVENT, DANA)));
    }

    @Test
    public void getResultsForEvent_returnsImmutableSnapshotMatchedByStableId() {
        model.updateParticipantRoleSearch("Speaker");
        List<Participation> snapshot = model.getParticipantSearchResultsForEvent(FIRST_EVENT);
        assertEquals(List.of("P10", "P2"), snapshot.stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals(snapshot, model.getParticipantSearchResultsForEvent(new Event(FIRST_ID, "Renamed", 10, "")));
        assertEquals(1, model.getParticipantSearchResultsForEvent(SECOND_EVENT).size());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(NullPointerException.class, () -> model.getParticipantSearchResultsForEvent(null));
        model.updateParticipantRoleSearch("Guest");
        assertEquals(2, snapshot.size());
        assertEquals(1, model.getParticipantSearchResultsForEvent(FIRST_EVENT).size());
    }

    @Test
    public void invalidSearches_preserveContextResultsAndEventFilter() {
        model.updateParticipantRoleSearch(FIRST_ID, "Speaker");
        List<Participation> before = List.copyOf(model.getParticipantSearchResults());
        for (String keyword : List.of("", " ", "\t\n", "\u2003", "\u00a0")) {
            assertThrows(IllegalArgumentException.class, () -> model.updateParticipantRoleSearch(keyword));
            assertThrows(IllegalArgumentException.class, () -> model.updateParticipantRoleSearch(FIRST_ID, keyword));
        }
        assertThrows(NullPointerException.class, () -> model.updateParticipantRoleSearch((String) null));
        assertThrows(NullPointerException.class, () -> model.updateParticipantRoleSearch(null, "Speaker"));
        assertThrows(NullPointerException.class, () -> model.updateParticipantRoleSearch(FIRST_ID, null));
        assertThrows(EventNotFoundException.class, () ->
                model.updateParticipantRoleSearch(new EventId("E99"), "Speaker"));
        assertEquals(before, model.getParticipantSearchResults());
        assertEquals(List.of(FIRST_EVENT), model.getFilteredEventList());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Speaker"), model.getParticipantSearchKeyword());
    }

    @Test
    public void globalSearch_observersSeeCurrentContextAndFilteredEvents() {
        model.updateParticipantRoleSearch(FIRST_ID, "Guest");
        AtomicInteger changes = new AtomicInteger();
        model.getParticipantSearchResults().addListener((ListChangeListener<Participation>) change -> {
            changes.incrementAndGet();
            assertTrue(model.getParticipantSearchEvent().isEmpty());
            assertEquals(Optional.of("Speaker"), model.getParticipantSearchKeyword());
            assertEquals(List.of(FIRST_EVENT, SECOND_EVENT), model.getFilteredEventList());
        });
        model.updateParticipantRoleSearch("Speaker");
        assertTrue(changes.get() > 0);
    }

    @Test
    public void existingRefreshHooks_keepGlobalAndScopedRolePredicates() {
        model.updateParticipantRoleSearch("Speaker");
        model.markPresent(FIRST_ID, BOB.getId());
        assertEquals(List.of("E2/P10", "E2/P2", "E1/P2"), resultIds());
        assertTrue(model.getParticipantSearchResults().getFirst().isPresent());
        assertTrue(model.getParticipantSearchEvent().isEmpty());

        model.updateParticipantRoleSearch(FIRST_ID, "Speaker");
        model.setPerson(BOB, new PersonBuilder(BOB).withName("Renamed Person").build());
        assertEquals(List.of("E2/P10", "E2/P2"), resultIds());
        assertEquals("Renamed Person", model.getParticipantSearchResults().getFirst().getPerson().getName().fullName);
    }

    @Test
    public void switchingToNameSearch_replacesRoleStateAndRestoresOrdinaryEventList() {
        model.updateParticipantRoleSearch("Missing");
        model.updateParticipantSearch(FIRST_ID, "  Alice  ");
        assertEquals(List.of("E2/P2"), resultIds());
        assertEquals(Optional.of(FIRST_EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Alice"), model.getParticipantSearchKeyword());
        assertEquals(List.of(FIRST_EVENT, SECOND_EVENT, EMPTY_EVENT), model.getFilteredEventList());
        model.setPresent(FIRST_EVENT, ALICE, true);
        assertEquals(List.of("E2/P2"), resultIds());
        assertTrue(model.getParticipantSearchResults().getFirst().isPresent());
        model.updateParticipantRoleSearch("Speaker");
        assertEquals(List.of("E2/P10", "E2/P2", "E1/P2"), resultIds());
    }

    @Test
    public void deletingEvents_dropsGlobalResultsAndClearsDeletedScope() {
        model.updateParticipantRoleSearch("Speaker");
        model.deleteEvent(FIRST_ID);
        assertEquals(List.of("E1/P2"), resultIds());
        assertEquals(List.of(SECOND_EVENT), model.getFilteredEventList());
        assertEquals(Optional.of("Speaker"), model.getParticipantSearchKeyword());

        model.updateParticipantRoleSearch(SECOND_ID, "Speaker");
        model.deleteEvent(SECOND_ID);
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertEquals(List.of(EMPTY_EVENT), model.getFilteredEventList());
        model.addParticipation(new EventId(EMPTY_EVENT.getId()), BOB.getId(), ALICE_ROLES, Set.of());
        assertTrue(model.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void roleSearch_preservesStoredDataAndAcceptsUnconstrainedSearchText() {
        AddressBook people = new AddressBook(model.getAddressBook());
        List<Event> events = model.getEvents();
        Set<Participation> participations = model.getParticipations().getParticipations();
        for (String keyword : List.of("Speaker", "?", "a".repeat(51))) {
            model.updateParticipantRoleSearch(keyword);
        }
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertEquals(people, model.getAddressBook());
        assertEquals(events, model.getEvents());
        assertEquals(participations, model.getParticipations().getParticipations());
    }

    @Test
    public void equals_includesSearchTypeEvenWhenQueryAndResultsAreIdentical() {
        ModelManager other = createModel();
        model.updateParticipantRoleSearch(FIRST_ID, "Missing");
        other.updateParticipantSearch(FIRST_ID, "Missing");
        other.updateFilteredEventList(event -> false);
        assertNotEquals(model, other);
        other.updateParticipantRoleSearch(FIRST_ID, "Missing");
        assertEquals(model, other);
    }

    private List<String> resultIds() {
        return model.getParticipantSearchResults().stream()
                .map(participation -> participation.getEvent().getId() + "/" + participation.getPerson().getId())
                .toList();
    }

    private ModelManager createModel() {
        ModelManager result = new ModelManager();
        for (Event event : List.of(FIRST_EVENT, SECOND_EVENT, ARCHIVED_EVENT, EMPTY_EVENT)) {
            result.addEvent(event);
        }
        for (Person person : List.of(ALICE, BOB, CHARLIE, DANA)) {
            result.addPerson(person);
        }
        result.addParticipation(FIRST_ID, ALICE.getId(), ALICE_ROLES, Set.of());
        result.addParticipation(FIRST_ID, BOB.getId(), Set.of(new ParticipationRole("Keynote Speaker")), Set.of());
        result.addParticipation(FIRST_ID, CHARLIE.getId(), Set.of(new ParticipationRole("Guest")), Set.of());
        result.addParticipation(FIRST_EVENT, DANA);
        result.addParticipation(SECOND_ID, ALICE.getId(), Set.of(new ParticipationRole("Speaker")), Set.of());
        result.addParticipation(new EventId(ARCHIVED_EVENT.getId()), CHARLIE.getId(), ALICE_ROLES, Set.of());
        return result;
    }
}
