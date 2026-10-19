package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AddEntryDialogTest {

    @Test
    public void buildAddPersonCommand_allFields_formatsExistingCliCommand() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30 t/friend t/volunteer",
                AddEntryDialog.buildAddPersonCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30",
                        "friend, volunteer"));
    }

    @Test
    public void buildAddPersonCommand_blankTags_omitsTagPrefixes() {
        assertEquals("add n/Alex Yeoh p/87438807 e/alex@example.com a/Blk 30",
                AddEntryDialog.buildAddPersonCommand("Alex Yeoh", "87438807", "alex@example.com", "Blk 30", " "));
    }

    @Test
    public void buildAddEventCommand_allFields_formatsExistingCliCommand() {
        assertEquals("add-event Orientation /cap 100 /desc Welcome session",
                AddEntryDialog.buildAddEventCommand("Orientation", "100", "Welcome session"));
    }
}
