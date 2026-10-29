package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.EventId;

public class SearchArgumentsParserTest {
    private static final String MESSAGE_INVALID_FORMAT = "Invalid search arguments.";

    @Test
    public void parse_globalSearch_normalizesKeywordAndPreservesArchiveSetting() throws Exception {
        for (boolean shouldIncludeArchived : List.of(false, true)) {
            String archiveFlag = shouldIncludeArchived ? "\u00a0/include-archive" : "";
            SearchArguments arguments = SearchArgumentsParser.parse(
                    "\u2003mIcHaEl\tTan/include-archive" + archiveFlag + "\u2003", MESSAGE_INVALID_FORMAT);

            assertEquals("mIcHaEl Tan/include-archive", arguments.getKeyword());
            assertEquals(Optional.empty(), arguments.getEventId());
            assertEquals(shouldIncludeArchived, arguments.shouldIncludeArchived());
        }
    }

    @Test
    public void parse_scopedSearch_normalizesKeywordAndPreservesScopeAndArchiveSetting() throws Exception {
        for (boolean shouldIncludeArchived : List.of(false, true)) {
            String archiveFlag = shouldIncludeArchived ? "\t/include-archive" : "";
            SearchArguments arguments = SearchArgumentsParser.parse(
                    "\t bEsT\nMan /event\u2003E001" + archiveFlag + "\u00a0", MESSAGE_INVALID_FORMAT);

            assertEquals("bEsT Man", arguments.getKeyword());
            assertEquals(Optional.of(new EventId("E001")), arguments.getEventId());
            assertEquals(shouldIncludeArchived, arguments.shouldIncludeArchived());
        }
    }
}
