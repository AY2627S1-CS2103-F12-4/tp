package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;
    private CommandResult expectedResult;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(
                getTypicalAddressBook(),
                new UserPrefs());

        expectedModel = new ModelManager(
                model.getAddressBook(),
                new UserPrefs());

        expectedResult = new CommandResult(
                ListCommand.MESSAGE_SUCCESS,
                false,
                false,
                false,
                false,
                true);
    }

    @Test
    public void execute_listIsNotFiltered_showsSameListAndPeopleTab() {
        assertCommandSuccess(
                new ListCommand(),
                model,
                expectedResult,
                expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverythingAndPeopleTab() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandSuccess(
                new ListCommand(),
                model,
                expectedResult,
                expectedModel);
    }
}
