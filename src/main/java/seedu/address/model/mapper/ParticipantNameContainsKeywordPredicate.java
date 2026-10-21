package seedu.address.model.mapper;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;

/**
 * Matches a participant's full name against a case-insensitive substring.
 * Event membership is scoped by the caller. Whitespace runs are normalized in names and queries.
 */
public class ParticipantNameContainsKeywordPredicate implements Predicate<Participation> {
    private final String keyword;

    /**
     * Creates a predicate for a nonblank search phrase, ignoring surrounding whitespace.
     * Words within the phrase must occur together in the supplied order.
     * Case normalization uses a fixed locale so results do not depend on the system locale.
     *
     * @param keyword Nonblank substring to find in a participant's full name.
     * @throws NullPointerException if the keyword is null.
     * @throws IllegalArgumentException if the keyword is blank.
     */
    public ParticipantNameContainsKeywordPredicate(String keyword) {
        requireNonNull(keyword);
        String normalizedKeyword = StringUtil.normalizeWhitespace(keyword);
        checkArgument(!normalizedKeyword.isEmpty(), "Participant search names must not be blank.");
        this.keyword = normalizedKeyword.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns whether the participant's full name contains the normalized search phrase.
     * Event membership is scoped by the caller; IDs, roles, and contact details are not searched.
     *
     * @param participation Record whose person's name is tested.
     * @return Whether the name contains the phrase, ignoring case.
     * @throws NullPointerException if the participation is null.
     */
    @Override
    public boolean test(Participation participation) {
        requireNonNull(participation);
        return StringUtil.normalizeWhitespace(participation.getPerson().getName().fullName)
                .toLowerCase(Locale.ROOT).contains(keyword);
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
