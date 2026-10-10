package seedu.address.model.event;

import static java.util.Objects.requireNonNull;

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
        this.keyword = requireNonNull(keyword);
    }

    @Override
    public boolean test(Event event) {
        String name = event.getName();
        String nameLowerCase = name.toLowerCase();
        String keywordLowerCase = this.keyword.toLowerCase();
        return nameLowerCase.contains(keywordLowerCase);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof EventNameContainsKeywordPredicate otherPredicate
                && keyword.equals(otherPredicate.keyword);
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }
}
