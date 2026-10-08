package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Objects;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.tag.Tag;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a person to the address book. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_ADDRESS + "ADDRESS "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "This person already exists in the address book.";

    private final Name name;
    private final Phone phone;
    private final Email email;
    private final Address address;
    private final Set<Tag> tags;

    /**
     * Creates an AddCommand using the specified person details.
     */
    public AddCommand(Name name, Phone phone, Email email,
                      Address address, Set<Tag> tags) {

        requireAllNonNull(name, phone, email, address, tags);

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags = Set.copyOf(tags);
    }

    /**
     * Creates an AddCommand using the details of the specified {@code Person}.
     */
    public AddCommand(Person person) {
        requireNonNull(person);

        this.name = person.getName();
        this.phone = person.getPhone();
        this.email = person.getEmail();
        this.address = person.getAddress();
        this.tags = Set.copyOf(person.getTags());
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        PersonId personId = model.generateNextPersonId();

        Person toAdd = new Person(
                personId,
                name,
                phone,
                email,
                address,
                new Remark(""),
                tags);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);

        return new CommandResult(
                String.format(
                        MESSAGE_SUCCESS,
                        Messages.format(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return name.equals(otherAddCommand.name)
                && phone.equals(otherAddCommand.phone)
                && email.equals(otherAddCommand.email)
                && address.equals(otherAddCommand.address)
                && tags.equals(otherAddCommand.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email, address, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .toString();
    }
}
