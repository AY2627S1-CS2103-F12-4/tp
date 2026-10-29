package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(
                getTypicalAddressBook(),
                new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(
                model.getAddressBook(),
                new UserPrefs());

        Person expectedPerson = new PersonBuilder(validPerson)
                .withId(expectedModel.generateNextPersonId().toString())
                .build();

        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(
                new AddCommand(validPerson),
                model,
                expectedMessage(expectedPerson),
                expectedModel);
    }

    @Test
    public void execute_adjacentEmailSymbols_success() throws Exception {
        String[] emails = {
            "alpha++tag@example.com",
            "alpha__tag@example.com",
            "alpha+-tag@example.com",
            "alpha_.tag@example.com",
            "alpha+tag@example.com"
        };

        for (String email : emails) {
            Model actualModel = new ModelManager();
            Model expectedModel = new ModelManager();

            Command command = new AddressBookParser().parseCommand(
                    "add n/Email Person p/91234567 e/"
                            + email
                            + " a/1 Main Street");

            Person expectedPerson = new PersonBuilder()
                    .withName("Email Person")
                    .withPhone("91234567")
                    .withEmail(email)
                    .withAddress("1 Main Street")
                    .withTags()
                    .withId(expectedModel.generateNextPersonId().toString())
                    .build();

            expectedModel.addPerson(expectedPerson);

            assertCommandSuccess(
                    command,
                    actualModel,
                    expectedMessage(expectedPerson),
                    expectedModel);
        }
    }

    @Test
    public void execute_personWithSameName_success() {
        Person personInList =
                model.getAddressBook().getPersonList().get(0);

        Person personWithSameName = new PersonBuilder(personInList)
                .withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB)
                .build();

        Model expectedModel = new ModelManager(
                model.getAddressBook(),
                new UserPrefs());

        Person expectedPerson = new PersonBuilder(personWithSameName)
                .withId(expectedModel.generateNextPersonId().toString())
                .build();

        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(
                new AddCommand(personWithSameName),
                model,
                expectedMessage(expectedPerson),
                expectedModel);
    }

    @Test
    public void execute_personWithSameDetails_success() {
        Person personInList =
                model.getAddressBook().getPersonList().get(0);

        Model expectedModel = new ModelManager(
                model.getAddressBook(),
                new UserPrefs());

        Person expectedPerson = new PersonBuilder(personInList)
                .withId(expectedModel.generateNextPersonId().toString())
                .build();

        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(
                new AddCommand(personInList),
                model,
                expectedMessage(expectedPerson),
                expectedModel);
    }

    private String expectedMessage(Person person) {
        return String.format(
                AddCommand.MESSAGE_SUCCESS,
                person.getId());
    }
}
