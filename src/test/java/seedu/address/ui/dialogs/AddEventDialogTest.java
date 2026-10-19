package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddEventDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() {
        assertEquals("add-event Orientation /cap 100 /desc Welcome session",
                AddEventDialog.buildCommand("Orientation", "100", "Welcome session"));
    }
}
