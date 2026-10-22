package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EditEventDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() {
        assertEquals("edit-event E1 Advanced Workshop /cap 40 /desc Advanced Java",
                EditEventDialog.buildCommand("E1", " Advanced Workshop ", " 40 ", " Advanced Java "));
    }

    @Test
    public void buildCommand_blankDescription_includesEmptyDescriptionPrefix() {
        assertEquals("edit-event E1 Workshop /cap 20 /desc ",
                EditEventDialog.buildCommand("E1", "Workshop", "20", " "));
    }
}
