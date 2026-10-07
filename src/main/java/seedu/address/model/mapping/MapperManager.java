package seedu.address.model.mapping;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapping.exceptions.DuplicateMappingException;
import seedu.address.model.mapping.exceptions.MappingNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Manages attendance mappings using event and person IDs as stable identifiers.
 */
public class MapperManager implements Mapper {

    private final EventList eventList;
    private final AddressBook addressBook;
    private final Map<MappingKey, AttendanceState> mappings = new HashMap<>();

    /**
     * Creates a mapper that resolves current events and people from the given
     * collections.
     * Both arguments must not be null.
     */
    public MapperManager(EventList eventList, AddressBook addressBook) {
        this(eventList, addressBook, Set.of());
    }

    /**
     * Creates a mapper initialized with the given attendance records.
     * Arguments and attendance records must not be null.
     *
     * @throws DuplicateMappingException if {@code attendances} contains duplicate
     *                                   mappings.
     */
    public MapperManager(EventList eventList, AddressBook addressBook, Collection<Attendance> attendances)
            throws DuplicateMappingException {
        this.eventList = requireNonNull(eventList);
        this.addressBook = requireNonNull(addressBook);
        requireNonNull(attendances);

        for (Attendance attendance : attendances) {
            requireNonNull(attendance);
            MappingKey key = createKey(attendance.getEvent(), attendance.getPerson());
            AttendanceState state = new AttendanceState(attendance.isPresent(), attendance.getTags());
            if (mappings.putIfAbsent(key, state) != null) {
                throw new DuplicateMappingException(key.eventId(), key.personId());
            }
        }
    }

    @Override
    public void addMapping(Event event, Person person) throws DuplicateMappingException {
        MappingKey key = createKey(event, person);
        if (mappings.containsKey(key)) {
            throw new DuplicateMappingException(key.eventId(), key.personId());
        }

        mappings.put(key, new AttendanceState(false, Set.of()));
    }

    @Override
    public void removeMapping(Event event, Person person) throws MappingNotFoundException {
        MappingKey key = createKey(event, person);
        if (mappings.remove(key) == null) {
            throw new MappingNotFoundException(key.eventId(), key.personId());
        }
    }

    @Override
    public Set<Attendance> getMappingsForEvent(Event event) throws EventNotFoundException {
        requireNonNull(event);
        String eventId = requireNonNull(event.getId());
        Event currentEvent = eventList.getEventFromId(eventId);
        if (currentEvent == null) {
            throw new EventNotFoundException();
        }

        return mappings.entrySet().stream()
                .filter(entry -> entry.getKey().eventId().equals(eventId))
                .flatMap(entry -> toAttendanceForEvent(
                        currentEvent, entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Attendance> getMappingsForPerson(Person person) throws PersonNotFoundException {
        requireNonNull(person);
        String personId = requireNonNull(person.getId());
        Person currentPerson = addressBook.getPersonFromId(personId);
        if (currentPerson == null) {
            throw new PersonNotFoundException();
        }

        return mappings.entrySet().stream()
                .filter(entry -> entry.getKey().personId().equals(personId))
                .flatMap(entry -> toAttendanceForPerson(
                        currentPerson, entry.getKey(), entry.getValue()).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Attendance setPresent(Event event, Person person, boolean isPresent)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException {
        MappingKey key = createKey(event, person);
        AttendanceState currentState = getState(key);
        AttendanceState updatedState = new AttendanceState(isPresent, currentState.tags());
        mappings.put(key, updatedState);
        return toAttendance(key, updatedState);
    }

    @Override
    public Attendance setTags(Event event, Person person, Set<Tag> tags)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException {
        MappingKey key = createKey(event, person);
        AttendanceState currentState = getState(key);
        AttendanceState updatedState = new AttendanceState(currentState.isPresent(), tags);
        mappings.put(key, updatedState);
        return toAttendance(key, updatedState);
    }

    @Override
    public void removeMappingsForEvent(Event event) {
        requireNonNull(event);
        String eventId = requireNonNull(event.getId());
        mappings.keySet().removeIf(key -> key.eventId().equals(eventId));
    }

    @Override
    public void removeMappingsForPerson(Person person) {
        requireNonNull(person);
        String personId = requireNonNull(person.getId());
        mappings.keySet().removeIf(key -> key.personId().equals(personId));
    }

    /**
     * Creates the stable key for an event-person pair.
     */
    private MappingKey createKey(Event event, Person person) {
        requireNonNull(event);
        requireNonNull(person);
        return new MappingKey(requireNonNull(event.getId()), requireNonNull(person.getId()));
    }

    /**
     * Returns the state for {@code key}.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     */
    private AttendanceState getState(MappingKey key) throws MappingNotFoundException {
        AttendanceState state = mappings.get(key);
        if (state == null) {
            throw new MappingNotFoundException(key.eventId(), key.personId());
        }
        return state;
    }

    /**
     * Returns an attendance record for {@code event}, or an empty result if its
     * person cannot be found.
     */
    private Optional<Attendance> toAttendanceForEvent(Event event, MappingKey key, AttendanceState state) {
        Person person = addressBook.getPersonFromId(key.personId());
        if (person == null) {
            return Optional.empty();
        }

        return Optional.of(new Attendance(event, person, state.isPresent(), state.tags()));
    }

    /**
     * Returns an attendance record for {@code person}, or an empty result if its
     * event cannot be found.
     */
    private Optional<Attendance> toAttendanceForPerson(Person person, MappingKey key, AttendanceState state) {
        Event event = eventList.getEventFromId(key.eventId());
        if (event == null) {
            return Optional.empty();
        }

        return Optional.of(new Attendance(event, person, state.isPresent(), state.tags()));
    }

    /**
     * Resolves the current event and person objects and creates an attendance
     * record.
     *
     * @throws EventNotFoundException if the referenced event cannot be found.
     * @throws PersonNotFoundException if the referenced person cannot be found.
     */
    private Attendance toAttendance(MappingKey key, AttendanceState state)
            throws EventNotFoundException, PersonNotFoundException {
        Event event = eventList.getEventFromId(key.eventId());
        Person person = addressBook.getPersonFromId(key.personId());
        if (event == null) {
            throw new EventNotFoundException();
        }
        if (person == null) {
            throw new PersonNotFoundException();
        }
        return new Attendance(event, person, state.isPresent(), state.tags());
    }

    /**
     * Identifies one event-person mapping.
     */
    private record MappingKey(String eventId, String personId) {
        private MappingKey {
            requireNonNull(eventId);
            requireNonNull(personId);
        }
    }

    /**
     * Stores the immutable attendance state associated with a mapping.
     */
    private record AttendanceState(boolean isPresent, Set<Tag> tags) {
        private AttendanceState {
            tags = Set.copyOf(tags);
        }
    }
}
