package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class EditPersonDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() throws ParseException {
        assertEquals("edit 2 n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street"
                        + " t/friend t/volunteer",
                EditPersonDialog.buildCommand(2, " Alice Tan ", " 91234567 ",
                        " alice@example.com ", " 1 Main Street ", " friend, volunteer "));
    }

    @Test
    public void buildCommand_blankTags_includesEmptyTagPrefix() throws ParseException {
        assertEquals("edit 2 n/Alice Tan p/91234567 e/alice@example.com a/1 Main Street t/",
                EditPersonDialog.buildCommand(2, "Alice Tan", "91234567",
                        "alice@example.com", "1 Main Street", " "));
    }
}
