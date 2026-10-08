package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_updatesRemark() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Remark remark = new Remark("Likes swimming.");
        CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, remark).execute(model);

        Person updated = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(remark, updated.getRemark());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getName(), updated.getName());
        assertEquals(original.getPhone(), updated.getPhone());
        assertEquals(original.getEmail(), updated.getEmail());
        assertEquals(original.getAddress(), updated.getAddress());
        assertEquals(original.getTags(), updated.getTags());
        assertEquals(String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(updated)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyRemark_clearsRemark() throws Exception {
        assertFalse(model.getFilteredPersonList().get(0).getRemark().value.isEmpty());
        CommandResult result = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")).execute(model);

        Person updated = model.getFilteredPersonList().get(0);
        assertEquals(new Remark(""), updated.getRemark());
        assertEquals(String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(updated)),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(invalidIndex, new Remark("hi")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person other = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        Remark remark = new Remark("Filtered remark.");
        new RemarkCommand(INDEX_FIRST_PERSON, remark).execute(model);

        assertEquals(remark, model.getAddressBook().getPersonList().get(1).getRemark());
        assertEquals(target.getId(), model.getAddressBook().getPersonList().get(1).getId());
        assertEquals(other.getRemark(), model.getAddressBook().getPersonList().get(0).getRemark());
        assertEquals(model.getAddressBook().getPersonList().size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_invalidFilteredIndex_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("hi")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("hi"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("hi"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("hi"))));
        assertFalse(command.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("bye"))));
    }
}
