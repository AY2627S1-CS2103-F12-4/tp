package seedu.address.ui.dialogs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.EditCommand;
import seedu.address.model.person.Person;

/** Collects replacement details for an existing person. */
public final class EditPersonDialog extends FormDialog {
    private final int displayedIndex;
    private final TextField name;
    private final TextField phone;
    private final TextField email;
    private final TextField address;
    private final TextField tags;

    /** Creates a pre-filled edit-person dialog for the supplied person. */
    public EditPersonDialog(Stage owner, Person person, int displayedIndex) {
        super(owner, "Edit person", "Edit " + person.getName().fullName + ".", "Edit");
        this.displayedIndex = displayedIndex;
        name = addTextField(0, "Name", "Full name", true);
        phone = addTextField(1, "Phone", "Phone number", true);
        email = addTextField(2, "Email", "Email address", true);
        address = addTextField(3, "Address", "Home address", true);
        tags = addTextField(4, "Tags (comma-separated)", "friend, volunteer", false);

        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value);
        address.setText(person.getAddress().value);
        tags.setText(person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> tag.tagName)
                .collect(Collectors.joining(", ")));
    }

    /** Displays the dialog and submits the replacement details through the command executor. */
    public void showAndWait(CommandExecutor commandExecutor) {
        showAndWait(name, commandExecutor, () -> buildCommand(displayedIndex,
                name.getText(), phone.getText(), email.getText(), address.getText(), tags.getText()));
    }

    static String buildCommand(int displayedIndex, String name, String phone, String email,
            String address, String tags) {
        String tagArguments = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> " t/" + tag)
                .collect(Collectors.joining());
        if (tagArguments.isEmpty()) {
            tagArguments = " t/";
        }
        return String.format("%s %d n/%s p/%s e/%s a/%s%s", EditCommand.COMMAND_WORD, displayedIndex,
                name.trim(), phone.trim(), email.trim(), address.trim(), tagArguments);
    }
}
