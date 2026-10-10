package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.MapperManager;
import seedu.address.model.mapping.Participation;
import seedu.address.model.mapping.ReadOnlyMappings;

/**
 * An immutable collection of participation mappings serializable to JSON.
 */
@JsonRootName(value = "mappings")
class JsonSerializableMappings {

    public static final String MESSAGE_DUPLICATE_MAPPING = "Mappings list contains duplicate mapping(s).";

    private final List<JsonAdaptedMapping> mappings = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableMappings} with the given mappings.
     */
    @JsonCreator
    public JsonSerializableMappings(@JsonProperty("mappings") List<JsonAdaptedMapping> mappings) {
        if (mappings != null) {
            this.mappings.addAll(mappings);
        }
    }

    /**
     * Converts the given mappings into this class for Jackson use.
     */
    public JsonSerializableMappings(ReadOnlyMappings source) {
        mappings.addAll(source.getMappings().stream().map(JsonAdaptedMapping::new).toList());
    }

    /**
     * Resolves all valid stored mappings against the current events and people.
     * Mappings with invalid or unknown IDs are omitted.
     */
    public ReadOnlyMappings toModelType(EventList eventList, AddressBook addressBook)
            throws IllegalValueException {
        List<Participation> modelMappings = new ArrayList<>();
        for (JsonAdaptedMapping mapping : mappings) {
            Participation participation = mapping.toModelType(eventList, addressBook).orElse(null);
            if (participation == null) {
                continue;
            }
            if (modelMappings.stream().anyMatch(participation::isSameMapping)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_MAPPING);
            }
            modelMappings.add(participation);
        }
        return new MapperManager(eventList, addressBook, modelMappings);
    }
}
