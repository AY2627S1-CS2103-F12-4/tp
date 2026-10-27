package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class EventNameContainsKeywordPredicateTest {

    @Test
    public void test_nameMatchesKeyword_returnsTrue() {
        Event event = new Event(new EventId("E1"), "Workshop", 10, "Java fundamentals");
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("Workshop");

        assertTrue(predicate.test(event));
    }

    // TODO: After green, add a nonmatching-name test.
    @Test
    public void test_nameNonMatchesKeyword_returnsFalse() {
        Event event = new Event(new EventId("E1"), "Workshop", 10, "Java fundamentals");
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("Shopwork");

        assertFalse(predicate.test(event));
    }
    // TODO: Decide case sensitivity and whole-word versus substring matching, then
    // test each rule.
    @Test
    public void test_caseInsensitiveMatchesKeyword_returnsTrue() {
        Event event = new Event(new EventId("E1"), "Workshop", 10, "Java fundamentals");
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("workshop");

        assertTrue(predicate.test(event));
    }

    @Test
    public void test_substringMatchesKeyword_returnsTrue() {
        Event event = new Event(new EventId("E1"), "Workshops", 10, "Java fundamentals");
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("shop");

        assertTrue(predicate.test(event));
    }


    // TODO: Verify that a keyword appearing only in the description does not match.
    @Test
    public void test_descriptionMatchesKeyword_returnsFalse() {
        Event event = new Event(new EventId("E1"), "Workshop", 10, "Shopwork fundamentals");
        EventNameContainsKeywordPredicate predicate = new EventNameContainsKeywordPredicate("Shopwork");

        assertFalse(predicate.test(event));
    }

    @Test
    public void test_turkishDefaultLocale_matchingIsLocaleIndependent() {
        assertCaseInsensitiveMatchesInLocale(Locale.forLanguageTag("tr-TR"));
    }

    @Test
    public void test_englishDefaultLocale_matchingIsCaseInsensitive() {
        assertCaseInsensitiveMatchesInLocale(Locale.forLanguageTag("en-SG"));
    }

    private void assertCaseInsensitiveMatchesInLocale(Locale locale) {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(locale);
            for (String name : List.of("DINNER", "dinner")) {
                Event event = new Event(new EventId("E1"), name, 10, "Locale reproduction");
                for (String keyword : List.of("i", "I", "dinner", "DINNER")) {
                    assertTrue(new EventNameContainsKeywordPredicate(keyword).test(event),
                            name + " should match " + keyword + " under " + locale);
                }
                assertFalse(new EventNameContainsKeywordPredicate("lunch").test(event));
            }
        } finally {
            Locale.setDefault(previousLocale);
        }
    }
}
