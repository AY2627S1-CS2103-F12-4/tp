package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.tag.Tag;

public class DialogInputValidatorTest {
    @TestFactory
    public Stream<DynamicTest> personAddress_eachRecognizedPrefix_isRejectedBeforeCommandConstruction() {
        return Stream.of(false, true).flatMap(edit ->
                Stream.of("n/Alice", "p/123", "e/other@example.com", "a/Other", "t/VIP").map(option ->
                        DynamicTest.dynamicTest((edit ? "Edit" : "Add") + " address with " + option, () -> {
                            String address = "3 Main Street " + option;
                            assertThrows(ParseException.class, DialogInputValidator.MESSAGE_ADDRESS_OPTIONS, () ->
                                    buildPersonCommand(edit, address));
                        })));
    }

    @TestFactory
    public Stream<DynamicTest> personAddress_safeSlashesAndOtherCommandPrefixes_remainLiteral() {
        return Stream.of(false, true).flatMap(edit ->
                Stream.of("#01-23, 10 A/B Road", "t/VIP Lane", "10/t/5 Lane", "10 /role Host Lane")
                        .map(address -> DynamicTest.dynamicTest((edit ? "Edit" : "Add") + " literal " + address, () -> {
                            String expectedStart = edit ? "edit 2" : "add";
                            assertEquals(expectedStart + " n/Alice Tan p/91234567 e/alice@example.com a/"
                                    + address + " t/friend", buildPersonCommand(edit, " " + address + " "));
                        })));
    }

    @Test
    public void participation_invalidValueAfterValidCsvValue_isRejected() {
        assertThrows(ParseException.class, ParticipationRole.MESSAGE_CONSTRAINTS, () ->
                DialogInputValidator.validateParticipation("Host, Speaker /tag VIP", "Guest"));
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                DialogInputValidator.validateParticipation("Host", "Guest, VIP /role Speaker"));
    }

    @Test
    public void person_invalidTagAfterValidCsvValue_isRejected() {
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                DialogInputValidator.validatePerson("Alice Tan", "91234567", "alice@example.com",
                        "1 Main Street", "friend, VIP t/Guest"));
    }

    private static String buildPersonCommand(boolean edit, String address) throws ParseException {
        if (edit) {
            return EditPersonDialog.buildCommand(2, "Alice Tan", "91234567", "alice@example.com", address, "friend");
        }
        return AddPersonDialog.buildCommand("Alice Tan", "91234567", "alice@example.com", address, "friend");
    }
}
