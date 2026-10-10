package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Participation} that stores entity IDs.
 */
class JsonAdaptedParticipation {

    private final String eventId;
    private final String personId;
    private final boolean isPresent;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<String> roles = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedParticipation} from its stored fields.
     */
    @JsonCreator
    public JsonAdaptedParticipation(@JsonProperty("eventId") String eventId,
                              @JsonProperty("personId") String personId,
                              @JsonProperty("isPresent") boolean isPresent,
                              @JsonProperty("tags") List<JsonAdaptedTag> tags,
                              @JsonProperty("roles") List<String> roles) {
        this.eventId = eventId;
        this.personId = personId;
        this.isPresent = isPresent;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /**
     * Converts a {@code Participation} into this class for Jackson use.
     */
    public JsonAdaptedParticipation(Participation source) {
        eventId = source.getEvent().getId();
        personId = source.getPerson().getId().toString();
        isPresent = source.isPresent();
        tags.addAll(source.getTags().stream().map(JsonAdaptedTag::new).toList());
        roles.addAll(source.getRoles().stream().map(ParticipationRole::toString).toList());
    }

    /**
     * Resolves the stored IDs and returns the corresponding participation.
     * Returns an empty result when either ID is invalid or cannot be found.
     */
    public Optional<Participation> toModelType(EventList eventList, AddressBook addressBook)
            throws IllegalValueException {
        if (!EventId.isValidEventId(eventId) || !PersonId.isValidPersonId(personId)) {
            return Optional.empty();
        }

        Event event;
        try {
            event = eventList.getEventFromId(eventId);
        } catch (EventNotFoundException e) {
            return Optional.empty();
        }

        Person person = addressBook.getPersonFromId(new PersonId(personId));
        if (person == null) {
            return Optional.empty();
        }

        Set<Tag> modelTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            modelTags.add(tag.toModelType());
        }

        Set<ParticipationRole> modelRoles = new HashSet<>();
        for (String role : roles) {
            if (!ParticipationRole.isValidRole(role)) {
                throw new IllegalValueException(ParticipationRole.MESSAGE_CONSTRAINTS);
            }
            modelRoles.add(new ParticipationRole(role));
        }

        return Optional.of(new Participation(event, person, isPresent, modelTags, modelRoles));
    }
}
