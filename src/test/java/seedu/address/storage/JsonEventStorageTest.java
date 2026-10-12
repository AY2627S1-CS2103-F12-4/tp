package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
