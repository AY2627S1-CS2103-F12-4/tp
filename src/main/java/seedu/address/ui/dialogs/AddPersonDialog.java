package seedu.address.ui.dialogs;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.parser.exceptions.ParseException;

/** Collects the parameters required to add a person. */
public final class AddPersonDialog extends FormDialog {
    private final TextField name;
    private final TextField phone;
    private final TextField email;
    private final TextField address;
    private final TextField tags;

    /** Creates an add-person dialog owned by the given stage. */
    public AddPersonDialog(Stage owner) {
        super(owner, "Add person", "Enter the person's details.");
        name = addTextField(0, "Name", "Full name", true);
        phone = addTextField(1, "Phone", "Phone number", true);
        email = addTextField(2, "Email", "Email address", true);
        address = addTextField(3, "Address", "Home address", true);
        tags = addTextField(4, "Tags (comma-separated)", "friend, volunteer", false);
    }

    /** Displays the dialog and submits valid person details through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(name, commandExecutor, () -> buildCommand(
                name.getText(), phone.getText(), email.getText(), address.getText(), tags.getText()));
    }

    static String buildCommand(String name, String phone, String email, String address, String tags)
            throws ParseException {
        DialogInputValidator.validatePerson(name, phone, email, address, tags);
        String tagArguments = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> " t/" + tag)
                .collect(Collectors.joining());
        return String.format("add n/%s p/%s e/%s a/%s%s", name.trim(), phone.trim(), email.trim(),
                address.trim(), tagArguments);
    }
}
