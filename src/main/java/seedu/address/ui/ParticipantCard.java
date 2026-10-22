package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.mapper.Participation;
import seedu.address.model.tag.Tag;

/**
 * Displays a participant's stable person ID, full name, tags,
 * event-specific roles, and actions.
 */
public class ParticipantCard extends UiPart<Region> {

    private static final String FXML = "ParticipantListCard.fxml";

    @FXML
    private Label personId;

    @FXML
    private Label name;

    @FXML
    private FlowPane tags;

    @FXML
    private Label roles;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    /**
     * Creates a card for the supplied participation.
     */
    public ParticipantCard(Participation participation) {
        this(participation, () -> { }, () -> { });
    }

    /**
     * Creates a participant card with edit and delete actions.
     */
    public ParticipantCard(
            Participation participation,
            Runnable editAction,
            Runnable deleteAction) {
        super(FXML);

        requireNonNull(participation);
        requireNonNull(editAction);
        requireNonNull(deleteAction);

        personId.setText(
                "Person ID: " + participation.getPerson().getId());

        name.setText(
                participation.getPerson().getName().fullName);

        roles.setText(formatRoles(participation));
        displayTags(participation);

        editButton.setOnAction(event -> editAction.run());
        deleteButton.setOnAction(event -> deleteAction.run());
    }

    private String formatRoles(Participation participation) {
        if (participation.getRoles().isEmpty()) {
            return "Roles: No role";
        }

        String formattedRoles = participation.getRoles().stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        return "Roles: " + formattedRoles;
    }

    /**
     * Displays the person's original tags and event-specific tags.
     */
    private void displayTags(Participation participation) {
        Set<Tag> personTags = participation.getPerson().getTags();

        personTags.stream()
                .sorted(Comparator.comparing(Tag::toString))
                .forEach(tag -> addTag(tag, "person-tag"));

        participation.getTags().stream()
                .filter(tag -> !personTags.contains(tag))
                .sorted(Comparator.comparing(Tag::toString))
                .forEach(tag -> addTag(tag, "event-tag"));
    }

    private void addTag(Tag tag, String styleClass) {
        Label tagLabel = new Label(tag.tagName);
        tagLabel.getStyleClass().add(styleClass);
        tags.getChildren().add(tagLabel);
    }
}
