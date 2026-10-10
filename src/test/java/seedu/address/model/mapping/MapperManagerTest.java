package seedu.address.model.mapping;

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
import seedu.address.model.mapping.exceptions.DuplicateMappingException;
import seedu.address.model.mapping.exceptions.MappingNotFoundException;
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
    public void addMapping_unknownEvent_throwsEventNotFoundException() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        MapperManager mapper = new MapperManager(new EventList(), addressBook);

        assertThrows(EventNotFoundException.class, () -> mapper.addMapping(event, person));
    }

    @Test
    public void addMapping_unknownPerson_throwsPersonNotFoundException() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);

        assertThrows(PersonNotFoundException.class, () -> mapper.addMapping(event, person));
    }

    @Test
    public void getMappings_multipleMappings_returnsUnmodifiableSnapshot() {
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
        mapper.addMapping(event, firstPerson);
        mapper.addMapping(event, secondPerson);
        mapper.setPresent(event, firstPerson, true);
        mapper.setTags(event, firstPerson, Set.of(new Tag("vip")));

        Set<Participation> mappings = mapper.getMappings();

        assertEquals(Set.of(
                new Participation(event, firstPerson, true, Set.of(new Tag("vip")), Set.of()),
                new Participation(event, secondPerson)), mappings);
        assertThrows(UnsupportedOperationException.class, () -> mappings.add(new Participation(event, firstPerson)));

        mapper.removeMapping(event, firstPerson);
        assertEquals(2, mappings.size());
    }

    @Test
    public void getMappings_missingReferences_omitsMappings() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addMapping(event, person);

        addressBook.removePerson(person);

        assertEquals(Set.of(), mapper.getMappings());
    }

    @Test
    public void getMappings_missingEvent_omitsMapping() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addMapping(event, person);

        eventList.removeEvent(event.getId());

        assertEquals(Set.of(), mapper.getMappings());
    }

    @Test
    public void getMappingsForPerson_missingEvent_omitsMapping() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addMapping(event, person);

        eventList.removeEvent(event.getId());

        assertEquals(Set.of(), mapper.getMappingsForPerson(person));
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

        assertEquals(Set.of(participation), restoredMapper.getMappingsForEvent(EVENT));
    }

    @Test
    public void constructor_duplicateParticipations_throwsDuplicateMappingException() {
        Participation first = new Participation(EVENT, PERSON);
        Participation duplicate = first.withPresent(true);

        DuplicateMappingException exception = assertThrows(DuplicateMappingException.class, () -> new MapperManager(
                eventList, addressBook, List.of(first, duplicate)));

        assertEquals("A mapping already exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void addMapping_newMapping_addsDefaultParticipation() {
        mapper.addMapping(EVENT, PERSON);

        Participation participation = mapper.getMappingsForEvent(EVENT).iterator().next();
        assertEquals(EVENT, participation.getEvent());
        assertEquals(PERSON, participation.getPerson());
        assertFalse(participation.isPresent());
        assertEquals(Set.of(), participation.getTags());
        assertEquals(Set.of(), participation.getRoles());
    }

    @Test
    public void addMapping_duplicateMapping_throwsDuplicateMappingException() {
        mapper.addMapping(EVENT, PERSON);

        DuplicateMappingException exception = assertThrows(
                DuplicateMappingException.class, () -> mapper.addMapping(EVENT, PERSON));

        assertEquals("A mapping already exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void addMapping_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.addMapping(null, PERSON));
        assertThrows(NullPointerException.class, () -> mapper.addMapping(EVENT, null));
    }

    @Test
    public void removeMapping_existingMapping_removesMapping() {
        mapper.addMapping(EVENT, PERSON);

        mapper.removeMapping(EVENT, PERSON);

        assertEquals(Set.of(), mapper.getMappingsForEvent(EVENT));
    }

    @Test
    public void removeMapping_missingMapping_throwsMappingNotFoundException() {
        MappingNotFoundException exception = assertThrows(
                MappingNotFoundException.class, () -> mapper.removeMapping(EVENT, PERSON));

        assertEquals("No mapping exists for event E1 and person P1", exception.getMessage());
    }

    @Test
    public void getMappingsForEvent_multipleMappings_returnsUnmodifiableSnapshot() {
        mapper.addMapping(EVENT, PERSON);
        mapper.addMapping(EVENT, OTHER_PERSON);

        Set<Participation> mappings = mapper.getMappingsForEvent(EVENT);
        mapper.removeMapping(EVENT, PERSON);

        assertEquals(2, mappings.size());
        assertThrows(UnsupportedOperationException.class, mappings::clear);
        assertEquals(1, mapper.getMappingsForEvent(EVENT).size());
    }

    @Test
    public void getMappingsForPerson_multipleMappings_returnsMatchingMappings() {
        mapper.addMapping(EVENT, PERSON);
        mapper.addMapping(OTHER_EVENT, PERSON);
        mapper.addMapping(EVENT, OTHER_PERSON);

        Set<Participation> mappings = mapper.getMappingsForPerson(PERSON);

        assertEquals(2, mappings.size());
        assertTrue(mappings.stream().allMatch(participation -> participation.getPerson().equals(PERSON)));
    }

    @Test
    public void getMappings_missingEntity_throwsNotFoundException() {
        Event missingEvent = new Event(new EventId("E3"), "Missing", 10, "Description");
        Person missingPerson = new PersonBuilder().withId("P3").build();

        assertThrows(EventNotFoundException.class, () -> mapper.getMappingsForEvent(missingEvent));
        assertThrows(PersonNotFoundException.class, () -> mapper.getMappingsForPerson(missingPerson));
    }

    @Test
    public void getMappingsForEvent_removedPerson_omitsStaleMapping() {
        mapper.addMapping(EVENT, PERSON);
        addressBook.removePerson(PERSON);

        assertEquals(Set.of(), mapper.getMappingsForEvent(EVENT));
    }

    @Test
    public void setPresent_existingMapping_updatesPresenceAndPreservesTags() {
        mapper.addMapping(EVENT, PERSON);
        mapper.setTags(EVENT, PERSON, Set.of(TAG));

        Participation updated = mapper.setPresent(EVENT, PERSON, true);

        assertTrue(updated.isPresent());
        assertEquals(Set.of(TAG), updated.getTags());
        assertEquals(Set.of(updated), mapper.getMappingsForEvent(EVENT));
    }

    @Test
    public void setTags_existingMapping_updatesTagsAndPreservesPresence() {
        mapper.addMapping(EVENT, PERSON);
        mapper.setPresent(EVENT, PERSON, true);

        Participation updated = mapper.setTags(EVENT, PERSON, Set.of(TAG));

        assertTrue(updated.isPresent());
        assertEquals(Set.of(TAG), updated.getTags());
    }

    @Test
    public void updateMapping_missingMapping_throwsMappingNotFoundException() {
        assertThrows(MappingNotFoundException.class, () -> mapper.setPresent(EVENT, PERSON, true));
        assertThrows(MappingNotFoundException.class, () -> mapper.setTags(EVENT, PERSON, Set.of(TAG)));
    }

    @Test
    public void updateMapping_removedEntities_throwsNotFoundException() {
        mapper.addMapping(EVENT, PERSON);
        eventList.removeEvent(EVENT.getId());
        assertThrows(EventNotFoundException.class, () -> mapper.setPresent(EVENT, PERSON, true));

        eventList.addEvent(EVENT);
        addressBook.removePerson(PERSON);
        assertThrows(PersonNotFoundException.class, () -> mapper.setTags(EVENT, PERSON, Set.of(TAG)));
    }

    @Test
    public void getMappings_resolvesCurrentEntitiesById() {
        mapper.addMapping(EVENT, PERSON);
        Event renamedEvent = new Event(new EventId("E1"), "Renamed Event", 25, "Updated");
        Person renamedPerson = new PersonBuilder(PERSON).withName("Renamed Person").build();
        eventList.removeEvent(EVENT.getId());
        eventList.addEvent(renamedEvent);
        addressBook.setPerson(PERSON, renamedPerson);

        Participation participation = mapper.getMappingsForEvent(EVENT).iterator().next();

        assertEquals(renamedEvent, participation.getEvent());
        assertEquals(renamedPerson, participation.getPerson());
    }

    @Test
    public void removeMappingsForEvent_matchingMappings_removesOnlyMatches() {
        mapper.addMapping(EVENT, PERSON);
        mapper.addMapping(EVENT, OTHER_PERSON);
        mapper.addMapping(OTHER_EVENT, PERSON);

        mapper.removeMappingsForEvent(EVENT);

        assertEquals(Set.of(), mapper.getMappingsForEvent(EVENT));
        assertEquals(1, mapper.getMappingsForEvent(OTHER_EVENT).size());
    }

    @Test
    public void removeMappingsForPerson_matchingMappings_removesOnlyMatches() {
        mapper.addMapping(EVENT, PERSON);
        mapper.addMapping(OTHER_EVENT, PERSON);
        mapper.addMapping(EVENT, OTHER_PERSON);

        mapper.removeMappingsForPerson(PERSON);

        assertEquals(Set.of(), mapper.getMappingsForPerson(PERSON));
        assertEquals(1, mapper.getMappingsForPerson(OTHER_PERSON).size());
    }

    @Test
    public void removeMappings_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> mapper.removeMappingsForEvent(null));
        assertThrows(NullPointerException.class, () -> mapper.removeMappingsForPerson(null));
    }
}
