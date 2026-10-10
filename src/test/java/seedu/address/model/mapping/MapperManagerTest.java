package seedu.address.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class MapperManagerTest {

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
    public void getMappingsForEvent_missingPerson_omitsMapping() {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addMapping(event, person);

        addressBook.removePerson(person);

        assertEquals(Set.of(), mapper.getMappingsForEvent(event));
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
}
