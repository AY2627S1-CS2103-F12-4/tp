package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ParticipantSearchDiagnosticsTest {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private static final EventId ACTIVE_ID = new EventId("E1");
    private static final EventId ARCHIVED_ID = new EventId("E2");
    private static final Person ALICE = new PersonBuilder().withId("P1").withName("Alice Tan").build();
    private static final Person ALICIA = new PersonBuilder().withId("P2").withName("Alicia Lee").build();
    private static final Set<ParticipationRole> SPEAKER_ROLES = Set.of(new ParticipationRole("Speaker"));

    private final RecordingHandler handler = new RecordingHandler();
    private ModelManager model;
    private Level originalLevel;
    private boolean originalUseParentHandlers;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addEvent(new Event(ACTIVE_ID, "Workshop", 20, ""));
        model.addEvent(new Event(ARCHIVED_ID, "Conference", 20, "", true));
        model.addPerson(ALICE);
        model.addPerson(ALICIA);
        model.addParticipation(ACTIVE_ID, ALICE.getId(), SPEAKER_ROLES, Set.of());
        model.addParticipation(ACTIVE_ID, ALICIA.getId(), SPEAKER_ROLES, Set.of());
        model.addParticipation(ARCHIVED_ID, ALICE.getId(), SPEAKER_ROLES, Set.of());

        originalLevel = logger.getLevel();
        originalUseParentHandlers = logger.getUseParentHandlers();
        handler.setLevel(Level.ALL);
        logger.setLevel(Level.FINER);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    @AfterEach
    public void tearDown() {
        logger.removeHandler(handler);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(originalUseParentHandlers);
        handler.close();
    }

    @Test
    public void nameSearch_globalScope_logsArchiveVisibilityAndDistinctEventCounts() {
        model.updateParticipantSearch("Ali");
        model.updateParticipantSearch("Ali", true);

        assertEquals(2, handler.records.size());
        assertSearchSummary(handler.records.get(0), "NAME", "ALL", false, 2, 1);
        assertSearchSummary(handler.records.get(1), "NAME", "ALL", true, 3, 2);
    }

    @Test
    public void roleSearch_archivedScope_logsZeroAndIncludedResults() {
        model.updateParticipantRoleSearch(ARCHIVED_ID, "Speaker");
        model.updateParticipantRoleSearch(ARCHIVED_ID, "Speaker", true);

        assertEquals(2, handler.records.size());
        assertSearchSummary(handler.records.get(0), "ROLE", "E2", false, 0, 0);
        assertSearchSummary(handler.records.get(1), "ROLE", "E2", true, 1, 1);
    }

    @Test
    public void refresh_activeRoleSearch_logsRefreshAndUpdatedCounts() {
        model.updateParticipantRoleSearch("Speaker", true);
        handler.records.clear();

        model.editParticipation(ACTIVE_ID, ALICE.getId(), Set.of(new ParticipationRole("Guest")), null);

        assertEquals(2, handler.records.size());
        assertEquals(Level.FINER, handler.records.get(0).getLevel());
        assertTrue(handler.records.get(0).getMessage().contains("Refreshing"));
        assertSearchSummary(handler.records.get(1), "ROLE", "ALL", true, 2, 2);

        model.clearParticipantSearch();
        handler.records.clear();
        model.editParticipation(ACTIVE_ID, ALICE.getId(), SPEAKER_ROLES, null);
        assertTrue(handler.records.isEmpty());
    }

    @Test
    public void invalidSearches_doNotLogAppliedSearch() {
        model.updateParticipantSearch("Ali");
        handler.records.clear();

        assertThrows(NullPointerException.class, () -> model.updateParticipantSearch((String) null));
        assertThrows(IllegalArgumentException.class, () -> model.updateParticipantRoleSearch(" "));
        assertThrows(EventNotFoundException.class, () ->
                model.updateParticipantSearch(new EventId("E99"), "Ali"));

        assertTrue(handler.records.isEmpty());
    }

    private void assertSearchSummary(LogRecord record, String type, String scope,
            boolean shouldIncludeArchived, int registrationCount, int eventCount) {
        assertEquals(Level.FINE, record.getLevel());
        String message = record.getMessage();
        for (String field : List.of("type=" + type, "scope=" + scope,
                "includeArchived=" + shouldIncludeArchived, "registrations=" + registrationCount,
                "events=" + eventCount)) {
            assertTrue(message.contains(field), () -> "Missing diagnostic field " + field + " in " + message);
        }
    }

    /** Records diagnostic messages without writing to the application's log handlers. */
    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (isLoggable(record)) {
                records.add(record);
            }
        }

        @Override
        public void flush() {
            // Records are kept in memory, so there is nothing to flush.
        }

        @Override
        public void close() {
            // This handler does not own external resources.
        }
    }
}
