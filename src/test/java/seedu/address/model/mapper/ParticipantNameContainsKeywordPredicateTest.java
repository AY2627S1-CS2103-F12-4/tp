package seedu.address.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ParticipantNameContainsKeywordPredicateTest {
    private static final Event EVENT = new Event(new EventId("E1"), "Wedding", 30, "Reception");
    private static final Person MICHAEL = new PersonBuilder().withId("P1").withName("Michael Tan").build();
    private static final Participation PARTICIPATION = new Participation(EVENT, MICHAEL);

    @Test
    public void constructor_nullKeyword_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ParticipantNameContainsKeywordPredicate(null));
    }

    @Test
    public void constructor_blankKeyword_throwsIllegalArgumentException() {
        for (String keyword : List.of("", " ", "\t\n", "\u2003")) {
            assertThrows(IllegalArgumentException.class, () -> new ParticipantNameContainsKeywordPredicate(keyword));
        }
    }

    @Test
    public void test_nullParticipation_throwsNullPointerException() {
        ParticipantNameContainsKeywordPredicate predicate = new ParticipantNameContainsKeywordPredicate("Michael");
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void test_exactAndPartialNames_returnsTrue() {
        for (String keyword : List.of("Michael Tan", "Michael", "Mich", "chae", "Tan", "an", "ael T")) {
            assertTrue(new ParticipantNameContainsKeywordPredicate(keyword).test(PARTICIPATION), keyword);
        }
    }

    @Test
    public void test_caseAndSurroundingWhitespace_ignored() {
        for (String keyword : List.of("MICHAEL", "mIcHaEl tAn", "  michael  ", "\tMichael\n",
                "Michael  Tan", "Michael\tTan", "Michael\u2003Tan")) {
            assertTrue(new ParticipantNameContainsKeywordPredicate(keyword).test(PARTICIPATION), keyword);
        }
    }

    @Test
    public void test_nonmatchingAndNoncontiguousPhrases_returnsFalse() {
        for (String keyword : List.of("Michelle", "Michael Tan Junior", "Tan Michael", "Michael an")) {
            assertFalse(new ParticipantNameContainsKeywordPredicate(keyword).test(PARTICIPATION), keyword);
        }
    }

    @Test
    public void test_repeatedWhitespaceInName_normalizedWithoutModifyingPerson() {
        Person person = new PersonBuilder(MICHAEL).withName("Michael  Tan").build();
        assertTrue(new ParticipantNameContainsKeywordPredicate("Michael Tan")
                .test(new Participation(EVENT, person)));
        assertEquals("Michael  Tan", person.getName().fullName);
    }

    @Test
    public void test_specialCharacters_treatedLiterally() {
        for (String keyword : List.of(".*", "Michael|Tan", "[", "Michael%")) {
            assertFalse(new ParticipantNameContainsKeywordPredicate(keyword).test(PARTICIPATION), keyword);
        }
    }

    @Test
    public void test_keywordOnlyInOtherFields_returnsFalse() {
        Person person = new PersonBuilder(MICHAEL).withAddress("Orchard Road")
                .withEmail("speaker@example.com").withPhone("91234567").withTags("friend").build();
        Participation participation = new Participation(EVENT, person, true,
                Set.of(new Tag("vip")), Set.of(new ParticipationRole("Best Man")));
        for (String keyword : List.of("P1", "E1", "Wedding", "Reception", "Best Man", "vip",
                "Orchard", "speaker", "91234567", "friend")) {
            assertFalse(new ParticipantNameContainsKeywordPredicate(keyword).test(participation), keyword);
        }
    }

    @Test
    public void test_turkishDefaultLocale_matchingIsLocaleIndependent() {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Person person = new PersonBuilder(MICHAEL).withName("IRIS").build();
            assertTrue(new ParticipantNameContainsKeywordPredicate("iris").test(new Participation(EVENT, person)));
            assertTrue(new ParticipantNameContainsKeywordPredicate("MICHAEL").test(PARTICIPATION));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    @Test
    public void test_sameNameAndRoleWithDifferentIds_preservesBothRecords() {
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Best Man"));
        Participation first = new Participation(EVENT, MICHAEL, false, Set.of(), roles);
        Person secondPerson = new PersonBuilder(MICHAEL).withId("P27").build();
        Participation second = new Participation(EVENT, secondPerson, false, Set.of(), roles);
        Set<Participation> participations = Set.of(first, second);

        List<Participation> matches = participations.stream()
                .filter(new ParticipantNameContainsKeywordPredicate("Michael")).toList();

        assertEquals(2, matches.size());
        assertEquals(Set.of("P1", "P27"), matches.stream()
                .map(participation -> participation.getPerson().getId().toString())
                .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    public void test_matchingAndNonmatchingSearches_doNotModifyRecord() {
        Set<Tag> tags = Set.of(new Tag("vip"));
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Best Man"));
        Participation participation = new Participation(EVENT, MICHAEL, true, tags, roles);

        assertTrue(new ParticipantNameContainsKeywordPredicate("Michael").test(participation));
        assertFalse(new ParticipantNameContainsKeywordPredicate("Michelle").test(participation));

        assertSame(EVENT, participation.getEvent());
        assertSame(MICHAEL, participation.getPerson());
        assertTrue(participation.isPresent());
        assertEquals(tags, participation.getTags());
        assertEquals(roles, participation.getRoles());
    }

    @Test
    public void equalsAndHashCode_normalizedKeyword() {
        ParticipantNameContainsKeywordPredicate predicate = new ParticipantNameContainsKeywordPredicate("Michael");
        ParticipantNameContainsKeywordPredicate equivalent = new ParticipantNameContainsKeywordPredicate(" MICHAEL ");
        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalent);
        assertEquals(equivalent, predicate);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertNotEquals(predicate, new ParticipantNameContainsKeywordPredicate("Tan"));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "Michael");
    }
}
