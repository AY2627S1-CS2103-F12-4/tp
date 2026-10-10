package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.MapperManager;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ReadOnlyParticipations;

/**
 * An immutable collection of participations serializable to JSON.
 */
@JsonRootName(value = "participations")
class JsonSerializableParticipations {

    public static final String MESSAGE_DUPLICATE_PARTICIPATION =
            "Participations list contains duplicate participation(s).";

    private final List<JsonAdaptedParticipation> participations = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableParticipations} with the given participations.
     */
    @JsonCreator
    public JsonSerializableParticipations(
            @JsonProperty("participations") List<JsonAdaptedParticipation> participations) {
        if (participations != null) {
            this.participations.addAll(participations);
        }
    }

    /**
     * Converts the given participations into this class for Jackson use.
     */
    public JsonSerializableParticipations(ReadOnlyParticipations source) {
        participations.addAll(source.getParticipations().stream().map(JsonAdaptedParticipation::new).toList());
    }

    /**
     * Resolves all valid stored participations against the current events and people.
     * Participations with invalid or unknown IDs are omitted.
     */
    public ReadOnlyParticipations toModelType(EventList eventList, AddressBook addressBook)
            throws IllegalValueException {
        List<Participation> modelParticipations = new ArrayList<>();
        for (JsonAdaptedParticipation adaptedParticipation : participations) {
            Participation modelParticipation = adaptedParticipation.toModelType(eventList, addressBook).orElse(null);
            if (modelParticipation == null) {
                continue;
            }
            if (modelParticipations.stream().anyMatch(modelParticipation::isSameParticipation)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PARTICIPATION);
            }
            modelParticipations.add(modelParticipation);
        }
        return new MapperManager(eventList, addressBook, modelParticipations);
    }
}
