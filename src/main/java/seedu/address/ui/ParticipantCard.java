package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.mapper.Participation;
import seedu.address.model.tag.Tag;

/**
 * Displays a participant's stable person ID, full name, tags, and
 * event-specific roles.
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

    /**
     * Creates a card for the supplied participation.
     *
     * @param participation event membership to display
     */
    public ParticipantCard(Participation participation) {
        super(FXML);
        requireNonNull(participation);

        personId.setText(
                "Person ID: " + participation.getPerson().getId());

        name.setText(
                participation.getPerson().getName().fullName);

        roles.setText(formatRoles(participation));

        displayTags(participation);
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
     * Displays both the person's original tags and event-specific tags.
     */
    private void displayTags(Participation participation) {
        Stream.concat(
                        participation.getPerson().getTags().stream(),
                        participation.getTags().stream())
                .distinct()
                .sorted(Comparator.comparing(Tag::toString))
                .forEach(tag ->
                        tags.getChildren().add(
                                new Label(tag.tagName)));
    }
}
