package seedu.address.logic.parser;

import java.util.List;

/**
 * Supplies shared invalid inputs for participant and role search parser tests.
 */
final class SearchParserTestData {
    private SearchParserTestData() {
    }

    /**
     * Returns blank search phrases, including whitespace variants and event scopes without a phrase.
     */
    static List<String> getBlankQueries() {
        return List.of("", " ", " \t ", "\u2003", "\u00a0", "/event", "/event E1", " /event E1",
                " \t\n /event E1");
    }

    /**
     * Returns incomplete, misplaced, and unsupported options for the supplied search phrase.
     */
    static List<String> getMalformedArguments(String keyword) {
        return List.of(keyword + " /event", keyword + " /event \u2003", keyword + " /event /event",
                keyword + " /eventE1", "/event E1 " + keyword, " /event E1 " + keyword,
                keyword + " /event E1 extra", keyword + " /Event E1", keyword + " /name Tan",
                keyword + " /name Alice", keyword + " /foo E1", "E1 /name " + keyword,
                keyword + " /role Speaker", "E1 /role " + keyword, "E1 /role" + keyword, keyword + " /");
    }

    /**
     * Returns duplicate event options, including repeated values and whitespace variants.
     */
    static List<String> getRepeatedEventArguments(String keyword) {
        return List.of(keyword + " /event E1 /event E2", keyword + " /event E1 /event E1",
                keyword + " /event /event E1", keyword + " /event E1 /event",
                keyword + " /event E1\t/event\tE2", keyword + " /event E1\t/event E2");
    }

    /**
     * Returns malformed or misplaced archive flags for the supplied search phrase.
     */
    static List<String> getMalformedArchiveArguments(String keyword) {
        return List.of("/include-archive", "/event E1 /include-archive", "/include-archive " + keyword,
                keyword + " /include-archive /event E1", keyword + " /include-archive extra",
                keyword + " /include-archive true", keyword + " /include-archive /include-archive",
                keyword + " /event E1 /include-archive /include-archive", keyword + " /include-archivex",
                keyword + " /Include-Archive", keyword + " /include-archive=true",
                keyword + " /event /include-archive", keyword + " /event E1 extra /include-archive",
                keyword + " /unknown /include-archive", keyword + " /event E1 /event E2 /include-archive");
    }

    /**
     * Returns invalid event IDs without changing the surrounding argument structure.
     */
    static List<String> getInvalidEventIds() {
        return List.of("1", "0", "-1", "+1", "1.5", "P1", "e1", "E", "E-1", "E1x");
    }
}
