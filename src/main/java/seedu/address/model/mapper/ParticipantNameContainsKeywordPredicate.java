package seedu.address.model.mapper;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * Matches a participant's full name against a case-insensitive substring.
 * Apply this predicate to the participations returned for the selected event.
 */
public class ParticipantNameContainsKeywordPredicate implements Predicate<Participation> {
    private final String keyword;

    /**
     * Creates a predicate for a nonblank search phrase, ignoring surrounding whitespace.
     * Words within the phrase must occur together in the supplied order.
     */
    public ParticipantNameContainsKeywordPredicate(String keyword) {
        requireNonNull(keyword);
        checkArgument(!keyword.isBlank(), "Participant search names must not be blank.");
        this.keyword = keyword.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean test(Participation participation) {
        requireNonNull(participation);
        return participation.getPerson().getName().fullName.toLowerCase(Locale.ROOT).contains(keyword);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof ParticipantNameContainsKeywordPredicate otherPredicate
                && keyword.equals(otherPredicate.keyword);
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }
}
