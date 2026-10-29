package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Displays a person's list index, permanent ID, contact details, tags and actions.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    public final Person person;

    @FXML
    private HBox cardPane;

    @FXML
    private Label name;

    @FXML
    private Label id;

    @FXML
    private Label personId;

    @FXML
    private Label phone;

    @FXML
    private Label address;

    @FXML
    private Label email;

    @FXML
    private FlowPane tags;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    /**
     * Creates a person card with its displayed index, permanent ID and actions.
     */
    public PersonCard(
            Person person,
            int displayedIndex,
            Runnable editAction,
            Runnable deleteAction) {
        super(FXML);

        this.person = requireNonNull(person);
        requireNonNull(editAction);
        requireNonNull(deleteAction);

        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        personId.setText("Person ID: " + person.getId());
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);

        displayTags(person);

        editButton.setOnAction(event -> editAction.run());
        deleteButton.setOnAction(event -> deleteAction.run());
    }

    private void displayTags(Person person) {
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> new Label(tag.tagName))
                .forEach(tags.getChildren()::add);
    }
}
