package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.testutil.TypicalPersons;

public class JsonEventStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveAndLoad_deletedHighestAndAllEvents_preservesRetiredIds() throws Exception {
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("events.json"));
        EventList events = new EventList();
        events.createEvent("First", 2, "");
        events.createEvent("Second", 2, "");
        events.removeEvent("E2");
        storage.saveEvents(events.getEvents(), events.getNextId());
        EventList loaded = storage.readEvents().orElseThrow();
        assertEquals("E3", loaded.createEvent("Third", 2, "").getId());
        loaded.removeEvent("E1");
        loaded.removeEvent("E3");
        storage.saveEvents(loaded.getEvents(), loaded.getNextId());
        loaded = storage.readEvents().orElseThrow();
        storage.saveEvents(loaded.getEvents(), loaded.getNextId());
        assertEquals("E4", storage.readEvents().orElseThrow().createEvent("Fourth", 2, "").getId());
    }

    @Test
    public void readEvents_legacyDocument_derivesNextIdAndSupportsLargeSequence() throws Exception {
        Path path = testFolder.resolve("events.json");
        Files.writeString(path, "{\"events\":[{\"id\":\"E7\",\"name\":\"Party\","
                + "\"capacity\":2,\"description\":\"\"}]}");
        JsonEventStorage storage = new JsonEventStorage(path);
        EventList loaded = storage.readEvents().orElseThrow();
        assertEquals("E8", loaded.createEvent("Next", 2, "").getId());
        BigInteger largeId = new BigInteger("999999999999999999999999999999");
        loaded.restoreNextId(largeId);
        storage.saveEvents(loaded.getEvents(), loaded.getNextId());
        assertEquals("E" + largeId, storage.readEvents().orElseThrow().createEvent("Large", 2, "").getId());
    }

    @Test
    public void readEvents_invalidSequence_throwsDataLoadingException() throws Exception {
        Path path = testFolder.resolve("events.json");
        JsonEventStorage storage = new JsonEventStorage(path);
        for (String id : List.of("0", "-1", "abc", "1.5", "1")) {
            Files.writeString(path, "{\"nextEventId\":\"" + id + "\",\"events\":[{\"id\":\"E1\","
                    + "\"name\":\"Party\",\"capacity\":2,\"description\":\"\"}]}");
            assertThrows(DataLoadingException.class, storage::readEvents);
        }
    }

    @Test
    public void readEvents_missingFile_returnsEmpty() throws Exception {
        assertFalse(new JsonEventStorage(testFolder.resolve("events.json")).readEvents().isPresent());
    }

    @Test
    public void saveAndLoad_events_preservesDetailsOrderAndNextId() throws Exception {
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("nested/events.json"));
        List<Event> events = List.of(new Event(new EventId("E20"), "迎新活动", 100, "Welcome\nSession"),
                new Event(new EventId("E3"), "迎新活动", 1, ""));
        storage.saveEvents(events);
        EventList loaded = storage.readEvents().orElseThrow();
        assertEquals(events, loaded.getEvents());
        assertEquals("E21", loaded.createEvent("Next", 10, "").getId());

        loaded.removeEvent("E3");
        storage.saveEvents(loaded.getEvents());
        assertEquals(loaded.getEvents(), storage.readEvents().orElseThrow().getEvents());
        storage.saveEvents(List.of());
        assertEquals(List.of(), storage.readEvents().orElseThrow().getEvents());
    }

    @Test
    public void readEvents_invalidData_throwsDataLoadingException() throws Exception {
        Path path = testFolder.resolve("events.json");
        JsonEventStorage storage = new JsonEventStorage(path);
        String valid = "{\"id\":\"E1\",\"name\":\"Party\",\"capacity\":10,\"description\":\"\"}";
        for (String json : List.of("not json", "{}", "{\"events\":null}", "{\"events\":[null]}",
                "{\"events\":[{}]}", "{\"events\":[" + valid + "," + valid + "]}",
                "{\"events\":[" + valid.replace("E1", "invalid") + "]}",
                "{\"events\":[" + valid.replace("Party", " ") + "]}",
                "{\"events\":[" + valid.replace(":10", ":0") + "]}")) {
            Files.writeString(path, json);
            assertThrows(DataLoadingException.class, storage::readEvents, json);
        }
    }

    @Test
    public void loadIntoModel_preservesPeopleEventsAndMapperReferences() throws Exception {
        JsonEventStorage storage = new JsonEventStorage(testFolder.resolve("events.json"));
        Event event = new Event(new EventId("E9"), "Party", 10, "");
        storage.saveEvents(List.of(event));
        ModelManager model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs(),
                storage.readEvents().orElseThrow());
        assertEquals(List.of(event), model.getFilteredEventList());
        model.addParticipation(event, TypicalPersons.ALICE);
        assertEquals(1, model.getParticipationsForEvent(event).size());
        model.updateFilteredEventList(unused -> false);
        assertEquals(List.of(event), model.getEvents());
        assertEquals("E10", model.createEvent("Next", 1, "").getId());
    }
}
