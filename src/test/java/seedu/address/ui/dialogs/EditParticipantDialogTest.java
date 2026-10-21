package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EditParticipantDialogTest {

    @Test
    public void buildCommand_rolesAndTags_formatsRepeatedCliArguments() {
        assertEquals("edit-participant E1 P1 /role Facilitator /role Guest Speaker /tag VIP /tag Alumni",
                EditParticipantDialog.buildCommand(
                        "E1", "P1", " Facilitator, Guest Speaker ", " VIP, Alumni "));
    }

    @Test
    public void buildCommand_blankRolesAndTags_includesEmptyPrefixes() {
        assertEquals("edit-participant E1 P1 /role /tag",
                EditParticipantDialog.buildCommand("E1", "P1", " ", " "));
    }
}
