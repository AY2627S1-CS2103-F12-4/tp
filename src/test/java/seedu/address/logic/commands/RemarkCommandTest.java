package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_throwsCommandException() {
        Remark remark = new Remark("Some remark");
        RemarkCommand remarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, remark);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ARGUMENTS,
                INDEX_FIRST_PERSON.getOneBased(), remark);

        assertCommandFailure(remarkCommand, model, expectedMessage);
    }

    @Test
    public void equals() {
        Remark amyRemark = new Remark(VALID_REMARK_AMY);
        Remark bobRemark = new Remark(VALID_REMARK_BOB);
        RemarkCommand remarkFirstCommand = new RemarkCommand(INDEX_FIRST_PERSON, amyRemark);
        RemarkCommand remarkSecondCommand = new RemarkCommand(INDEX_SECOND_PERSON, amyRemark);
        RemarkCommand differentRemarkCommand = new RemarkCommand(INDEX_FIRST_PERSON, bobRemark);

        // same object -> returns true
        assertTrue(remarkFirstCommand.equals(remarkFirstCommand));

        // same values -> returns true
        RemarkCommand remarkFirstCommandCopy = new RemarkCommand(INDEX_FIRST_PERSON,
                new Remark(VALID_REMARK_AMY));
        assertTrue(remarkFirstCommand.equals(remarkFirstCommandCopy));

        // different types -> returns false
        assertFalse(remarkFirstCommand.equals(1));

        // null -> returns false
        assertFalse(remarkFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(remarkFirstCommand.equals(remarkSecondCommand));

        // different remark -> returns false
        assertFalse(remarkFirstCommand.equals(differentRemarkCommand));
    }
}
