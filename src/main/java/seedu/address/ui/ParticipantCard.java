package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.mapper.Participation;

/**
 * Displays a participant's stable person ID, full name, and event-specific roles.
 */
public class ParticipantCard extends UiPart<Region> {
    private static final String FXML = "ParticipantListCard.fxml";

    @FXML
    private Label personId;
    @FXML
    private Label name;
    @FXML
    private Label roles;

    /**
     * Creates a card for the supplied participation without substituting a list index for its ID.
     * Displays roles in alphabetical order, or "No role" when none are assigned.
     *
     * @param participation Event membership supplying the person and roles to display.
     * @throws NullPointerException if the participation is null.
     */
    public ParticipantCard(Participation participation) {
        super(FXML);
        requireNonNull(participation);
        personId.setText("Person ID: " + participation.getPerson().getId());
        name.setText(participation.getPerson().getName().fullName);
        roles.setText("Roles: " + (participation.getRoles().isEmpty() ? "No role"
                : participation.getRoles().stream().map(Object::toString).sorted()
                        .collect(Collectors.joining(", "))));
    }
}
