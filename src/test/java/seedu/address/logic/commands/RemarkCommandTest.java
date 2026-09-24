package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class RemarkCommandTest {
    private ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute() {
        assertCommandFailure(new RemarkCommand(), model, RemarkCommand.MESSAGE_NOT_IMPLEMENTED_YET);
    }
}
