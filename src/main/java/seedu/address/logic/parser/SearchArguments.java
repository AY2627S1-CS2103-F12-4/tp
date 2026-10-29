package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.event.EventId;

/**
 * Contains a normalized search phrase, optional event scope, and archive setting.
 */
final class SearchArguments {
    private final String keyword;
    private final Optional<EventId> eventId;
    private final boolean shouldIncludeArchived;

    /**
     * Creates immutable arguments for a participant or role search.
     */
    SearchArguments(String keyword, Optional<EventId> eventId, boolean shouldIncludeArchived) {
        this.keyword = requireNonNull(keyword);
        this.eventId = requireNonNull(eventId);
        this.shouldIncludeArchived = shouldIncludeArchived;
    }

    String getKeyword() {
        return keyword;
    }

    Optional<EventId> getEventId() {
        return eventId;
    }

    boolean shouldIncludeArchived() {
        return shouldIncludeArchived;
    }
}
