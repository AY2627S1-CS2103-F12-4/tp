package seedu.address.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.ParticipationNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class MapperManagerTest {
    private static final Event EVENT = new Event(new EventId("E1"), "Workshop", 20, "Description");
    private static final Event OTHER_EVENT = new Event(new EventId("E2"), "Seminar", 30, "Description");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();
    private static final Person OTHER_PERSON = new PersonBuilder().withId("P2").withName("Bob Bee").build();
    private static final Tag TAG = new Tag("vip");

    private EventList eventList;
    private AddressBook addressBook;
    private MapperManager mapper;

    @BeforeEach
    public void setUp() {
        eventList = new EventList();
        eventList.addEvent(EVENT);
        eventList.addEvent(OTHER_EVENT);
        addressBook = new AddressBook();
        addressBook.addPerson(PERSON);
        addressBook.addPerson(OTHER_PERSON);
        mapper = new MapperManager(eventList, addressBook);
    }

    @Test
    public void addParticipation_unknownEvent_throwsEventNotFoundException() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        MapperManager mapper = new MapperManager(new EventList(), addressBook);

        assertThrows(EventNotFoundException.class, () -> mapper.addParticipation(event, person));
    }

    @Test
    public void addParticipation_unknownPerson_throwsPersonNotFoundException() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);

        assertThrows(PersonNotFoundException.class, () -> mapper.addParticipation(event, person));
    }

    @Test
    public void getParticipations_multipleParticipations_returnsUnmodifiableSnapshot() {
        Person firstPerson = new PersonBuilder().withId("P1").build();
        Person secondPerson = new PersonBuilder().withId("P2")
                .withName("Bob Bee")
                .withPhone("91234567")
                .withEmail("bob@example.com")
                .build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(firstPerson);
        addressBook.addPerson(secondPerson);

        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);

        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addParticipation(event, firstPerson);
        mapper.addParticipation(event, secondPerson);
        mapper.setPresent(event, firstPerson, true);
        mapper.setTags(event, firstPerson, Set.of(new Tag("vip")));

        Set<Participation> participations = mapper.getParticipations();

        assertEquals(Set.of(
                new Participation(event, firstPerson, true, Set.of(new Tag("vip")), Set.of()),
                new Participation(event, secondPerson)), participations);
        assertThrows(UnsupportedOperationException.class, () ->
                participations.add(new Participation(event, firstPerson)));

        mapper.removeParticipation(event, firstPerson);
        assertEquals(2, participations.size());
    }

    @Test
    public void getParticipations_missingReferences_omitsParticipations() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addParticipation(event, person);

        addressBook.removePerson(person);

        assertEquals(Set.of(), mapper.getParticipations());
    }

    @Test
    public void getParticipations_missingEvent_omitsParticipation() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addParticipation(event, person);

        eventList.removeEvent(event.getId());

        assertEquals(Set.of(), mapper.getParticipations());
    }

    @Test
    public void getParticipationsForPerson_missingEvent_omitsParticipation() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addParticipation(event, person);

        eventList.removeEvent(event.getId());

        assertEquals(Set.of(), mapper.getParticipationsForPerson(person));
    }

    @Test
    public void constructor_nullDependency_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MapperManager(null, addressBook));
        assertThrows(NullPointerException.class, () -> new MapperManager(eventList, null));
        assertThrows(NullPointerException.class, () -> new MapperManager(eventList, addressBook, null));
        assertThrows(NullPointerException.class, () -> new MapperManager(
                eventList, addressBook, Arrays.asList((Participation) null)));
    }

    @Test
    public void constructor_participations_restoresState() {
        Participation participation = new Participation(EVENT, PERSON, true, Set.of(TAG),
                Set.of(new ParticipationRole("Speaker")));

        MapperManager restoredMapper = new MapperManager(eventList, addressBook, List.of(participation));

        assertEquals(Set.of(participation), restoredMapper.getParticipationsForEvent(EVENT));
    }

    @Test
    public void constructor_duplicateParticipations_throwsDuplicateParticipationException() {
        Participation first = new Participation(EVENT, PERSON);
        Participation duplicate = first.withPresent(true);

        DuplicateParticipationException exception = assertThrows(DuplicateParticipationException.class, () ->
                new MapperManager(eventList, addressBook, List.of(first, duplicate)));

        assertEquals("A participation already exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void addParticipation_newParticipation_addsDefaultParticipation() {
        mapper.addParticipation(EVENT, PERSON);

        Participation participation = mapper.getParticipationsForEvent(EVENT).iterator().next();
        assertEquals(EVENT, participation.getEvent());
        assertEquals(PERSON, participation.getPerson());
        assertFalse(participation.isPresent());
        assertEquals(Set.of(), participation.getTags());
        assertEquals(Set.of(), participation.getRoles());
    }

    @Test
    public void addParticipation_duplicateParticipation_throwsDuplicateParticipationException() {
        mapper.addParticipation(EVENT, PERSON);

        DuplicateParticipationException exception = assertThrows(
                DuplicateParticipationException.class, () -> mapper.addParticipation(EVENT, PERSON));

        assertEquals("A participation already exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void addParticipation_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.addParticipation(null, PERSON));
        assertThrows(NullPointerException.class, () -> mapper.addParticipation(EVENT, null));
    }

    @Test
    public void removeParticipation_existingParticipation_removesParticipation() {
        mapper.addParticipation(EVENT, PERSON);

        mapper.removeParticipation(EVENT, PERSON);

        assertEquals(Set.of(), mapper.getParticipationsForEvent(EVENT));
    }

    @Test
    public void removeParticipation_missingParticipation_throwsParticipationNotFoundException() {
        ParticipationNotFoundException exception = assertThrows(
                ParticipationNotFoundException.class, () -> mapper.removeParticipation(EVENT, PERSON));

        assertEquals("No participation exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void getParticipationsForEvent_multipleParticipations_returnsUnmodifiableSnapshot() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.addParticipation(EVENT, OTHER_PERSON);

        Set<Participation> participations = mapper.getParticipationsForEvent(EVENT);
        mapper.removeParticipation(EVENT, PERSON);

        assertEquals(2, participations.size());
        assertThrows(UnsupportedOperationException.class, participations::clear);
        assertEquals(1, mapper.getParticipationsForEvent(EVENT).size());
    }

    @Test
    public void getParticipationsForPerson_multipleParticipations_returnsMatchingParticipations() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.addParticipation(OTHER_EVENT, PERSON);
        mapper.addParticipation(EVENT, OTHER_PERSON);

        Set<Participation> participations = mapper.getParticipationsForPerson(PERSON);

        assertEquals(2, participations.size());
        assertTrue(participations.stream().allMatch(participation -> participation.getPerson().equals(PERSON)));
    }

    @Test
    public void getParticipations_missingEntity_throwsNotFoundException() {
        Event missingEvent = new Event(new EventId("E3"), "Missing", 10, "Description");
        Person missingPerson = new PersonBuilder().withId("P3").build();

        assertThrows(EventNotFoundException.class, () -> mapper.getParticipationsForEvent(missingEvent));
        assertThrows(PersonNotFoundException.class, () -> mapper.getParticipationsForPerson(missingPerson));
    }

    @Test
    public void getParticipationsForEvent_removedPerson_omitsStaleParticipation() {
        mapper.addParticipation(EVENT, PERSON);
        addressBook.removePerson(PERSON);

        assertEquals(Set.of(), mapper.getParticipationsForEvent(EVENT));
    }

    @Test
    public void setPresent_existingParticipation_updatesPresenceAndPreservesTags() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.setTags(EVENT, PERSON, Set.of(TAG));

        Participation updated = mapper.setPresent(EVENT, PERSON, true);

        assertTrue(updated.isPresent());
        assertEquals(Set.of(TAG), updated.getTags());
        assertEquals(Set.of(updated), mapper.getParticipationsForEvent(EVENT));
    }

    @Test
    public void setTags_existingParticipation_updatesTagsAndPreservesPresence() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.setPresent(EVENT, PERSON, true);

        Participation updated = mapper.setTags(EVENT, PERSON, Set.of(TAG));

        assertTrue(updated.isPresent());
        assertEquals(Set.of(TAG), updated.getTags());
    }

    @Test
    public void updateParticipation_missingParticipation_throwsParticipationNotFoundException() {
        assertThrows(ParticipationNotFoundException.class, () -> mapper.setPresent(EVENT, PERSON, true));
        assertThrows(ParticipationNotFoundException.class, () -> mapper.setTags(EVENT, PERSON, Set.of(TAG)));
    }

    @Test
    public void updateParticipation_removedEntities_throwsNotFoundException() {
        mapper.addParticipation(EVENT, PERSON);
        eventList.removeEvent(EVENT.getId());
        assertThrows(EventNotFoundException.class, () -> mapper.setPresent(EVENT, PERSON, true));

        eventList.addEvent(EVENT);
        addressBook.removePerson(PERSON);
        assertThrows(PersonNotFoundException.class, () -> mapper.setTags(EVENT, PERSON, Set.of(TAG)));
    }

    @Test
    public void getParticipations_resolvesCurrentEntitiesById() {
        mapper.addParticipation(EVENT, PERSON);
        Event renamedEvent = new Event(new EventId("E1"), "Renamed Event", 25, "Updated");
        Person renamedPerson = new PersonBuilder(PERSON).withName("Renamed Person").build();
        eventList.removeEvent(EVENT.getId());
        eventList.addEvent(renamedEvent);
        addressBook.setPerson(PERSON, renamedPerson);

        Participation participation = mapper.getParticipationsForEvent(EVENT).iterator().next();

        assertEquals(renamedEvent, participation.getEvent());
        assertEquals(renamedPerson, participation.getPerson());
    }

    @Test
    public void removeParticipationsForEvent_matchingParticipations_removesOnlyMatches() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.addParticipation(EVENT, OTHER_PERSON);
        mapper.addParticipation(OTHER_EVENT, PERSON);

        mapper.removeParticipationsForEvent(EVENT);

        assertEquals(Set.of(), mapper.getParticipationsForEvent(EVENT));
        assertEquals(1, mapper.getParticipationsForEvent(OTHER_EVENT).size());
    }

    @Test
    public void removeParticipationsForPerson_matchingParticipations_removesOnlyMatches() {
        mapper.addParticipation(EVENT, PERSON);
        mapper.addParticipation(OTHER_EVENT, PERSON);
        mapper.addParticipation(EVENT, OTHER_PERSON);

        mapper.removeParticipationsForPerson(PERSON);

        assertEquals(Set.of(), mapper.getParticipationsForPerson(PERSON));
        assertEquals(1, mapper.getParticipationsForPerson(OTHER_PERSON).size());
    }

    @Test
    public void removeParticipations_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.removeParticipationsForEvent(null));
        assertThrows(NullPointerException.class, () -> mapper.removeParticipationsForPerson(null));
    }
}
