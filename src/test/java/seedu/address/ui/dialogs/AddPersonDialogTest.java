package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddPersonDialogTest {

    @Test
    public void buildCommand_allFields_formatsExistingCliCommand() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30 t/friend t/volunteer",
                AddPersonDialog.buildCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30",
                        "friend, volunteer"));
    }

    @Test
    public void buildCommand_blankTags_omitsTagPrefixes() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30",
                AddPersonDialog.buildCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30", " "));
    }
}
