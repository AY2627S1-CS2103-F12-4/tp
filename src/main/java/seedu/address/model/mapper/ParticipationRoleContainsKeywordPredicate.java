package seedu.address.model.mapper;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * Tests whether any role of a {@code Participation} contains the complete search phrase.
 * Matching ignores case and uses a substring rather than whole-word comparison.
 * Participations without roles never match.
 */
public class ParticipationRoleContainsKeywordPredicate implements Predicate<Participation> {
    private final String keyword;

    /**
     * Creates a predicate with leading and trailing whitespace removed, internal whitespace
     * collapsed to single spaces, and case normalized independently of the default locale.
     * The keyword is search text and does not need to satisfy assigned-role constraints.
     *
     * @param keyword The complete phrase to search for within each role.
     * @throws NullPointerException if {@code keyword} is null.
     * @throws IllegalArgumentException if {@code keyword} is blank.
     */
    public ParticipationRoleContainsKeywordPredicate(String keyword) {
        requireNonNull(keyword);
        checkArgument(!keyword.isBlank(), "Role search keyword must not be blank.");
        this.keyword = keyword.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether at least one assigned role contains the normalized search phrase.
     * A phrase must match within one role; separate roles are not combined.
     *
     * @throws NullPointerException if {@code participation} is null.
     */
    @Override
    public boolean test(Participation participation) {
        requireNonNull(participation);
        return participation.getRoles().stream()
                .anyMatch(role -> role.value.toLowerCase(Locale.ROOT).contains(keyword));
    }

    /**
     * Returns whether the other predicate has the same normalized search phrase.
     */
    @Override
    public boolean equals(Object other) {
        return other == this
                || other instanceof ParticipationRoleContainsKeywordPredicate otherPredicate
                && keyword.equals(otherPredicate.keyword);
    }

    /**
     * Returns the hash code of the normalized search phrase.
     */
    @Override
    public int hashCode() {
        return keyword.hashCode();
    }
}
