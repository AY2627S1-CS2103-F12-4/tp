package seedu.address.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests role search matching, input validation, and normalized predicate equality.
 */
public class ParticipationRoleContainsKeywordPredicateTest {
    private static final Event EVENT = new Event(new EventId("E1"), "Wedding", 20, "Celebration");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();

    @Test
    public void constructor_nullKeyword_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ParticipationRoleContainsKeywordPredicate(null));
    }

    @Test
    public void constructor_blankKeyword_throwsIllegalArgumentException() {
        for (String keyword : new String[] {"", " ", "\t\n", "\u2003"}) {
            assertThrows(IllegalArgumentException.class, () -> new ParticipationRoleContainsKeywordPredicate(keyword));
        }
    }

    @Test
    public void test_exactRole_returnsTrue() {
        assertTrue(new ParticipationRoleContainsKeywordPredicate("Best Man")
                .test(participationWithRoles("Best Man")));
    }

    @Test
    public void test_substringWithinWord_returnsTrue() {
        assertTrue(new ParticipationRoleContainsKeywordPredicate("sher")
                .test(participationWithRoles("Usher")));
    }

    @Test
    public void test_differentCase_returnsTrue() {
        assertTrue(new ParticipationRoleContainsKeywordPredicate("bEsT mAn")
                .test(participationWithRoles("BEST MAN")));
    }

    @Test
    public void test_extraWhitespace_normalizesQueryAndMatchesRole() {
        assertTrue(new ParticipationRoleContainsKeywordPredicate(" \tBest \t\n Man\n ")
                .test(participationWithRoles("  Best   Man  ")));
    }

    @Test
    public void test_nonmatchingRole_returnsFalse() {
        assertFalse(new ParticipationRoleContainsKeywordPredicate("Best Man")
                .test(participationWithRoles("Usher")));
    }

    @Test
    public void test_multipleRoles_matchesAnyRole() {
        Participation participation = participationWithRoles("Usher", "Best Man", "Assistant Best Man");

        assertTrue(new ParticipationRoleContainsKeywordPredicate("Best Man").test(participation));
        assertFalse(new ParticipationRoleContainsKeywordPredicate("Speaker").test(participation));
    }

    @Test
    public void test_phraseRequiresContiguousMatchWithinOneRole() {
        ParticipationRoleContainsKeywordPredicate predicate =
                new ParticipationRoleContainsKeywordPredicate("Best Man");

        assertFalse(predicate.test(participationWithRoles("Best", "Man")));
        assertFalse(predicate.test(participationWithRoles("Best Assistant Man")));
        assertFalse(predicate.test(participationWithRoles("Man Best")));
    }

    @Test
    public void test_noRoles_returnsFalse() {
        assertFalse(new ParticipationRoleContainsKeywordPredicate("Best Man")
                .test(new Participation(EVENT, PERSON)));
    }

    @Test
    public void test_matchingNonRoleFields_returnsFalse() {
        Event event = new Event(new EventId("E2"), "Best Man", 20, "Best Man");
        Person person = new PersonBuilder().withId("P2").withName("Best Man").build();
        Participation participation = new Participation(event, person, true,
                Set.of(new Tag("BestMan")), Set.of(new ParticipationRole("Usher")));

        assertFalse(new ParticipationRoleContainsKeywordPredicate("Best Man").test(participation));
        assertFalse(new ParticipationRoleContainsKeywordPredicate("BestMan").test(participation));
    }

    @Test
    public void test_queryOutsideAssignedRoleConstraints_returnsFalseWithoutThrowing() {
        Participation participation = participationWithRoles("Usher");

        assertFalse(new ParticipationRoleContainsKeywordPredicate("?").test(participation));
        assertFalse(new ParticipationRoleContainsKeywordPredicate("a".repeat(51)).test(participation));
    }

    @Test
    public void test_turkishDefaultLocale_matchesIndependentlyOfLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertTrue(new ParticipationRoleContainsKeywordPredicate("assistant")
                    .test(participationWithRoles("ASSISTANT")));
            assertTrue(new ParticipationRoleContainsKeywordPredicate("ASSISTANT")
                    .test(participationWithRoles("assistant")));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_nullParticipation_throwsNullPointerException() {
        ParticipationRoleContainsKeywordPredicate predicate = new ParticipationRoleContainsKeywordPredicate("Usher");
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void equals_normalizedKeyword_comparesSearchMeaning() {
        ParticipationRoleContainsKeywordPredicate predicate =
                new ParticipationRoleContainsKeywordPredicate("Best Man");
        ParticipationRoleContainsKeywordPredicate equivalent =
                new ParticipationRoleContainsKeywordPredicate("  BEST \t MAN  ");

        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalent);
        assertEquals(equivalent, predicate);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertNotEquals(predicate, new ParticipationRoleContainsKeywordPredicate("Usher"));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "Best Man");
    }

    /**
     * Creates a participation with the supplied roles using assigned-role normalization.
     */
    private static Participation participationWithRoles(String... roles) {
        Set<ParticipationRole> participationRoles = Arrays.stream(roles)
                .map(ParticipationRole::new)
                .collect(Collectors.toSet());
        return new Participation(EVENT, PERSON, false, Set.of(), participationRoles);
    }
}
