package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
