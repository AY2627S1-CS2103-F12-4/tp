package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonEventStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicParticipantSearchTest {
    @TempDir
    public Path temporaryFolder;

    private final Event event = new Event(new EventId("E1"), "Wedding", 20, "Reception");
    private final Person person = new PersonBuilder().withId("P1").withName("Michael Tan").build();
    private ModelManager model;
    private Logic logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addEvent(event);
        model.addPerson(person);
        model.addParticipation(event, person);
        logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"))));
    }

    @Test
    public void getSearchState_beforeSearch_returnsEmptyView() {
        assertTrue(logic.getParticipantSearchResults().isEmpty());
        assertEquals(Optional.empty(), logic.getParticipantSearchEvent());
        assertEquals(Optional.empty(), logic.getParticipantSearchKeyword());
    }

    @Test
    public void execute_searchAndMutation_exposesLiveUnmodifiableView() throws Exception {
        ObservableList<Participation> results = logic.getParticipantSearchResults();
        logic.execute("search-participant Michael /event E1");
        assertSame(model.getParticipantSearchResults(), results);
        assertEquals(1, results.size());
        assertEquals(Optional.of(event), logic.getParticipantSearchEvent());
        assertEquals(Optional.of("Michael"), logic.getParticipantSearchKeyword());
        assertThrows(UnsupportedOperationException.class, results::clear);
        logic.execute("mark-participant E1 P1");
        assertTrue(results.getFirst().isPresent());
        logic.execute("del-participant E1 P1");
        assertTrue(results.isEmpty());
        assertSame(results, logic.getParticipantSearchResults());
    }

    @Test
    public void execute_invalidSearch_preservesExistingView() throws Exception {
        logic.execute("search-participant Michael /event E1");
        List<Participation> before = List.copyOf(logic.getParticipantSearchResults());
        assertThrows(ParseException.class, () -> logic.execute("search-participant Michael /event"));
        assertEquals(before, logic.getParticipantSearchResults());
        assertEquals(Optional.of(event), logic.getParticipantSearchEvent());
        assertEquals(Optional.of("Michael"), logic.getParticipantSearchKeyword());
    }

    @Test
    public void execute_globalSearch_exposesPerEventResultsAndNormalizedContext() throws Exception {
        logic.execute("search-participant Michael   Tan");
        assertEquals(List.of(event), logic.getFilteredEventList());
        assertEquals(logic.getParticipantSearchResults(), logic.getParticipantSearchResultsForEvent(event));
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertEquals(Optional.of("Michael Tan"), logic.getParticipantSearchKeyword());
    }

    @Test
    public void execute_listAndFindEvents_clearParticipantSearchAndKeepTheirFilters() throws Exception {
        Event other = model.createEvent("Other", 10, "");
        logic.execute("search-participant Missing");
        assertTrue(logic.getFilteredEventList().isEmpty());
        logic.execute("list-event");
        assertEquals(List.of(event, other), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchKeyword().isEmpty());

        logic.execute("search-participant Michael /event E1");
        logic.execute("find-event Other");
        assertEquals(List.of(other), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchResults().isEmpty());
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertTrue(logic.getParticipantSearchKeyword().isEmpty());
        model.addParticipation(other, person);
        assertEquals(List.of(other), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void getResultsForEvent_globalRoleSearch_returnsMatchingSnapshotsThatCanBeRefreshed() throws Exception {
        Event other = model.createEvent("Workshop", 10, "");
        model.editParticipation(new EventId(event.getId()), person.getId(),
                Set.of(new ParticipationRole("Speaker")), null);
        model.addParticipation(new EventId(other.getId()), person.getId(),
                Set.of(new ParticipationRole("Speaker")), Set.of());
        logic.execute("search-role Speaker");

        List<Participation> snapshot = logic.getParticipantSearchResultsForEvent(event);
        assertEquals(1, snapshot.size());
        assertEquals(event, snapshot.getFirst().getEvent());
        assertEquals(other, logic.getParticipantSearchResultsForEvent(other).getFirst().getEvent());
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertEquals(Optional.of("Speaker"), logic.getParticipantSearchKeyword());

        logic.execute("edit-participant E1 P1 /role Guest");
        assertTrue(logic.getParticipantSearchResultsForEvent(event).isEmpty());
        assertEquals(1, snapshot.size());
        assertEquals(1, logic.getParticipantSearchResultsForEvent(other).size());
        assertEquals(List.of(other), logic.getFilteredEventList());
    }

    @Test
    public void execute_search_savedDataPreservedAndSearchStateNotPersisted() throws Exception {
        logic.execute("search-participant Missing /event E1");
        JsonAddressBookStorage peopleStorage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonEventStorage eventStorage = new JsonEventStorage(temporaryFolder.resolve("events.json"));
        JsonParticipationStorage participationStorage =
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"));
        AddressBook savedPeople = new AddressBook(peopleStorage.readAddressBook().orElseThrow());
        EventList savedEvents = eventStorage.readEvents().orElseThrow();
        ModelManager restored = new ModelManager(savedPeople, new UserPrefs(), savedEvents,
                participationStorage.readParticipations(savedEvents, savedPeople).orElseThrow());
        assertEquals(model.getAddressBook(), restored.getAddressBook());
        assertEquals(model.getEvents(), restored.getEvents());
        assertEquals(model.getParticipations().getParticipations(), restored.getParticipations().getParticipations());
        assertTrue(restored.getParticipantSearchResults().isEmpty());
        assertTrue(restored.getParticipantSearchEvent().isEmpty());
        assertTrue(restored.getParticipantSearchKeyword().isEmpty());
    }
}
