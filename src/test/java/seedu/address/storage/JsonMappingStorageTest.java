package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.MapperManager;
import seedu.address.model.mapping.Participation;
import seedu.address.model.mapping.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonMappingStorageTest {

    @TempDir
    public Path testFolder;

    private AddressBook addressBook;
    private Event event;
    private EventList eventList;
    private Person person;

    @BeforeEach
    public void setUp() {
        person = new PersonBuilder().withId("P1").build();
        addressBook = new AddressBook();
        addressBook.addPerson(person);

        event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        eventList = new EventList();
        eventList.addEvent(event);
    }

    @Test
    public void readMappings_missingFile_emptyResult() throws Exception {
        JsonMappingStorage storage = new JsonMappingStorage(testFolder.resolve("missing.json"));

        assertFalse(storage.readMappings(eventList, addressBook).isPresent());
    }

    @Test
    public void readMappings_invalidJson_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalid.json");
        Files.writeString(filePath, "not json");
        JsonMappingStorage storage = new JsonMappingStorage(filePath);

        assertThrows(DataLoadingException.class, () -> storage.readMappings(eventList, addressBook));
    }

    @Test
    public void readMappings_unknownOrInvalidIds_dropsMappings() throws Exception {
        Path filePath = testFolder.resolve("mappings.json");
        Files.writeString(filePath, """
                {
                  "mappings" : [ {
                    "eventId" : "E1",
                    "personId" : "P1",
                    "isPresent" : true,
                    "tags" : [ "vip" ],
                    "roles" : [ "Speaker" ]
                  }, {
                    "eventId" : "E999",
                    "personId" : "P1",
                    "isPresent" : false,
                    "tags" : [ ],
                    "roles" : [ ]
                  }, {
                    "eventId" : "invalid",
                    "personId" : "P1",
                    "isPresent" : false,
                    "tags" : [ ],
                    "roles" : [ ]
                  }, {
                    "eventId" : "E1",
                    "personId" : "P999",
                    "isPresent" : false,
                    "tags" : [ ],
                    "roles" : [ ]
                  }, {
                    "eventId" : "E1",
                    "personId" : "invalid",
                    "isPresent" : false,
                    "tags" : [ ],
                    "roles" : [ ]
                  } ]
                }
                """);
        JsonMappingStorage storage = new JsonMappingStorage(filePath);

        List<Participation> mappings = storage.readMappings(eventList, addressBook).orElseThrow();

        assertEquals(List.of(new Participation(event, person, true,
                Set.of(new Tag("vip")), Set.of(new ParticipationRole("Speaker")))), mappings);
    }

    @Test
    public void saveAndReadMappings_allDataPreservedAndReferencesStoredAsIds() throws Exception {
        Path filePath = testFolder.resolve("mappings.json");
        JsonMappingStorage storage = new JsonMappingStorage(filePath);
        Participation original = new Participation(event, person, true,
                Set.of(new Tag("vip")), Set.of(new ParticipationRole("Speaker")));
        MapperManager mapper = new MapperManager(eventList, addressBook, List.of(original));

        storage.saveMappings(mapper);
        List<Participation> readBack = storage.readMappings(eventList, addressBook).orElseThrow();
        String json = Files.readString(filePath);

        assertEquals(List.of(original), readBack);
        assertTrue(json.contains("\"eventId\" : \"E1\""));
        assertTrue(json.contains("\"personId\" : \"P1\""));
        assertFalse(json.contains("Orientation"));
        assertFalse(json.contains(person.getName().toString()));
    }

    @Test
    public void readMappings_duplicateMapping_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("duplicate.json");
        Files.writeString(filePath, """
                {
                  "mappings" : [ {
                    "eventId" : "E1",
                    "personId" : "P1"
                  }, {
                    "eventId" : "E1",
                    "personId" : "P1"
                  } ]
                }
                """);
        JsonMappingStorage storage = new JsonMappingStorage(filePath);

        assertThrows(DataLoadingException.class, () -> storage.readMappings(eventList, addressBook));
    }

    @Test
    public void saveMappings_nullMappings_throwsNullPointerException() {
        JsonMappingStorage storage = new JsonMappingStorage(testFolder.resolve("mappings.json"));

        assertThrows(NullPointerException.class, () -> storage.saveMappings(null));
    }
}
