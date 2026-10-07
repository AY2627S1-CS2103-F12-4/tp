package seedu.address.model.event;

import java.util.function.Predicate;

/**
 * Tests whether an event name matches the keyword supplied to find-event.
 */
public class EventNameContainsKeywordPredicate implements Predicate<Event> {
    private final String keyword;

    /**
     * Creates a predicate for the supplied keyword.
     */
    public EventNameContainsKeywordPredicate(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean test(Event event) {
        String name = event.getName();
        String nameLowerCase = name.toLowerCase();
        String keywordLowerCase = this.keyword.toLowerCase();
        return nameLowerCase.contains(keywordLowerCase);
    }
}
