package seedu.address.model.mapper;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.ParticipationNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Manages participations using event and person IDs as stable identifiers.
 */
public class MapperManager implements Mapper {

    private final EventList eventList;
    private final AddressBook addressBook;
    private final Map<ParticipationKey, ParticipationState> participations = new HashMap<>();

    /**
     * Creates a mapper that resolves current events and people from the given
     * collections.
     */
    public MapperManager(EventList eventList, AddressBook addressBook) {
        this(eventList, addressBook, Set.of());
    }

    /**
     * Creates a mapper initialized with the given participation records.
     *
     * @throws DuplicateParticipationException if {@code participations} contains
     *                                   duplicate participations.
     */
    public MapperManager(EventList eventList, AddressBook addressBook,
                         Collection<Participation> participations)
            throws DuplicateParticipationException {

        this.eventList = requireNonNull(eventList);
        this.addressBook = requireNonNull(addressBook);
        requireNonNull(participations);

        for (Participation participation : participations) {
            requireNonNull(participation);

            ParticipationKey key = createKey(
                    participation.getEvent(),
                    participation.getPerson());

            ParticipationState state = new ParticipationState(
                    participation.isPresent(),
                    participation.getTags(),
                    participation.getRoles());

            if (this.participations.putIfAbsent(key, state) != null) {
                throw new DuplicateParticipationException(
                        key.eventId(),
                        key.personId().toString());
            }
        }
    }

    @Override
    public void addParticipation(Event event, Person person)
            throws DuplicateParticipationException {

        ParticipationKey key = createKey(event, person);
        eventList.getEventFromId(key.eventId());
        if (addressBook.getPersonFromId(key.personId()) == null) {
            throw new PersonNotFoundException();
        }

        if (participations.containsKey(key)) {
            throw new DuplicateParticipationException(
                    key.eventId(),
                    key.personId().toString());
        }

        participations.put(
                key,
                new ParticipationState(
                        false,
                        Set.of(),
                        Set.of()));
    }

    @Override
    public void removeParticipation(Event event, Person person)
            throws ParticipationNotFoundException {

        ParticipationKey key = createKey(event, person);

        if (participations.remove(key) == null) {
            throw new ParticipationNotFoundException(
                    key.eventId(),
                    key.personId().toString());
        }
    }

    @Override
    public Set<Participation> getParticipations() {
        Set<Participation> validParticipations = new HashSet<>();
        for (Map.Entry<ParticipationKey, ParticipationState> entry : participations.entrySet()) {
            try {
                validParticipations.add(toParticipation(entry.getKey(), entry.getValue()));
            } catch (EventNotFoundException | PersonNotFoundException e) {
                continue;
            }
        }
        return Set.copyOf(validParticipations);
    }

    @Override
    public Set<Participation> getParticipationsForEvent(Event event)
            throws EventNotFoundException {

        requireNonNull(event);

        String eventId = requireNonNull(event.getId());
        eventList.getEventFromId(eventId);

        Set<Participation> eventParticipations = new HashSet<>();
        for (Map.Entry<ParticipationKey, ParticipationState> entry : participations.entrySet()) {
            if (!entry.getKey().eventId().equals(eventId)) {
                continue;
            }
            try {
                eventParticipations.add(toParticipation(entry.getKey(), entry.getValue()));
            } catch (PersonNotFoundException e) {
                continue;
            }
        }
        return Set.copyOf(eventParticipations);
    }

    @Override
    public Set<Participation> getParticipationsForPerson(Person person)
            throws PersonNotFoundException {

        requireNonNull(person);

        PersonId personId = requireNonNull(person.getId());
        if (addressBook.getPersonFromId(personId) == null) {
            throw new PersonNotFoundException();
        }

        Set<Participation> personParticipations = new HashSet<>();
        for (Map.Entry<ParticipationKey, ParticipationState> entry : participations.entrySet()) {
            if (!entry.getKey().personId().equals(personId)) {
                continue;
            }
            try {
                personParticipations.add(toParticipation(entry.getKey(), entry.getValue()));
            } catch (EventNotFoundException e) {
                continue;
            }
        }
        return Set.copyOf(personParticipations);
    }

    @Override
    public Participation setPresent(
            Event event,
            Person person,
            boolean isPresent)
            throws ParticipationNotFoundException,
            EventNotFoundException,
            PersonNotFoundException {

        ParticipationKey key = createKey(event, person);
        ParticipationState currentState = getState(key);

        ParticipationState updatedState =
                new ParticipationState(
                        isPresent,
                        currentState.tags(),
                        currentState.roles());

        participations.put(key, updatedState);

        return toParticipation(key, updatedState);
    }

    @Override
    public Participation setTags(
            Event event,
            Person person,
            Set<Tag> tags)
            throws ParticipationNotFoundException,
            EventNotFoundException,
            PersonNotFoundException {

        ParticipationKey key = createKey(event, person);
        ParticipationState currentState = getState(key);

        ParticipationState updatedState =
                new ParticipationState(
                        currentState.isPresent(),
                        tags,
                        currentState.roles());

        participations.put(key, updatedState);

        return toParticipation(key, updatedState);
    }

    @Override
    public void removeParticipationsForEvent(Event event) {
        requireNonNull(event);

        String eventId = requireNonNull(event.getId());

        participations.keySet()
                .removeIf(key ->
                        key.eventId().equals(eventId));
    }

    @Override
    public void removeParticipationsForPerson(Person person) {
        requireNonNull(person);

        PersonId personId = requireNonNull(person.getId());

        participations.keySet()
                .removeIf(key ->
                        key.personId().equals(personId));
    }

    /**
     * Creates the stable key for an event-person pair.
     */
    private ParticipationKey createKey(Event event, Person person) {
        requireNonNull(event);
        requireNonNull(person);

        return new ParticipationKey(
                requireNonNull(event.getId()),
                requireNonNull(person.getId()));
    }

    /**
     * Returns the state for {@code key}.
     */
    private ParticipationState getState(ParticipationKey key)
            throws ParticipationNotFoundException {

        ParticipationState state = participations.get(key);

        if (state == null) {
            throw new ParticipationNotFoundException(
                    key.eventId(),
                    key.personId().toString());
        }

        return state;
    }

    /**
     * Resolves the current event and person and creates a participation.
     */
    private Participation toParticipation(
            ParticipationKey key,
            ParticipationState state)
            throws EventNotFoundException,
            PersonNotFoundException {

        Event event = eventList.getEventFromId(key.eventId());
        Person person = addressBook.getPersonFromId(key.personId());

        if (person == null) {
            throw new PersonNotFoundException();
        }

        return new Participation(
                event,
                person,
                state.isPresent(),
                state.tags(),
                state.roles());
    }

    /**
     * Identifies one event-person pair.
     */
    private record ParticipationKey(
            String eventId,
            PersonId personId) {

        private ParticipationKey {
            requireNonNull(eventId);
            requireNonNull(personId);
        }
    }

    /**
     * Stores the immutable state associated with a participation.
     */
    private record ParticipationState(
            boolean isPresent,
            Set<Tag> tags,
            Set<ParticipationRole> roles) {

        private ParticipationState {
            tags = Set.copyOf(tags);
            roles = Set.copyOf(roles);
        }
    }
}
