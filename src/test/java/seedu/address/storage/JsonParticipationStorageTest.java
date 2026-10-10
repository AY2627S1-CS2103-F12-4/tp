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
import seedu.address.model.mapper.MapperManager;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class JsonParticipationStorageTest {

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
    public void readParticipations_missingFile_emptyResult() throws Exception {
        JsonParticipationStorage storage = new JsonParticipationStorage(testFolder.resolve("missing.json"));

        assertFalse(storage.readParticipations(eventList, addressBook).isPresent());
    }

    @Test
    public void readParticipations_invalidJson_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("invalid.json");
        Files.writeString(filePath, "not json");
        JsonParticipationStorage storage = new JsonParticipationStorage(filePath);

        assertThrows(DataLoadingException.class, () -> storage.readParticipations(eventList, addressBook));
    }

    @Test
    public void readParticipations_unknownOrInvalidIds_dropsParticipations() throws Exception {
        Path filePath = testFolder.resolve("participations.json");
        Files.writeString(filePath, """
                {
                  "participations" : [ {
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
        JsonParticipationStorage storage = new JsonParticipationStorage(filePath);

        ReadOnlyParticipations participations = storage.readParticipations(eventList, addressBook).orElseThrow();

        assertEquals(Set.of(new Participation(event, person, true,
                Set.of(new Tag("vip")), Set.of(new ParticipationRole("Speaker")))), participations.getParticipations());
    }

    @Test
    public void saveAndReadParticipations_allDataPreservedAndReferencesStoredAsIds() throws Exception {
        Path filePath = testFolder.resolve("participations.json");
        JsonParticipationStorage storage = new JsonParticipationStorage(filePath);
        Participation original = new Participation(event, person, true,
                Set.of(new Tag("vip")), Set.of(new ParticipationRole("Speaker")));
        MapperManager mapper = new MapperManager(eventList, addressBook, List.of(original));

        storage.saveParticipations(mapper);
        ReadOnlyParticipations readBack = storage.readParticipations(eventList, addressBook).orElseThrow();
        String json = Files.readString(filePath);

        assertEquals(Set.of(original), readBack.getParticipations());
        assertTrue(json.contains("\"eventId\" : \"E1\""));
        assertTrue(json.contains("\"personId\" : \"P1\""));
        assertFalse(json.contains("Orientation"));
        assertFalse(json.contains(person.getName().toString()));
    }

    @Test
    public void readParticipations_duplicateParticipation_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("duplicate.json");
        Files.writeString(filePath, """
                {
                  "participations" : [ {
                    "eventId" : "E1",
                    "personId" : "P1"
                  }, {
                    "eventId" : "E1",
                    "personId" : "P1"
                  } ]
                }
                """);
        JsonParticipationStorage storage = new JsonParticipationStorage(filePath);

        assertThrows(DataLoadingException.class, () -> storage.readParticipations(eventList, addressBook));
    }

    @Test
    public void saveParticipations_nullParticipations_throwsNullPointerException() {
        JsonParticipationStorage storage = new JsonParticipationStorage(testFolder.resolve("participations.json"));

        assertThrows(NullPointerException.class, () -> storage.saveParticipations(null));
    }
}
