package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.event.Event;

/**
 * Displays an event's details and its index in the current search results.
 */
public class EventCard extends UiPart<Region> {
    private static final String FXML = "EventListCard.fxml";

    @FXML
    private Label index;
    @FXML
    private Label name;
    @FXML
    private Label eventId;
    @FXML
    private Label capacity;
    @FXML
    private Label description;
    @FXML
    private Button deleteButton;

    /**
     * Creates an event card with its displayed index and deletion action.
     */
    public EventCard(Event event, int displayedIndex, Runnable deleteAction) {
        super(FXML);
        requireNonNull(deleteAction);
        index.setText(displayedIndex + ". ");
        name.setText(event.getName());
        eventId.setText("Event ID: " + event.getId());
        capacity.setText("Capacity: " + event.getCapacity());
        description.setText(event.getDescription());
        description.setVisible(!event.getDescription().isBlank());
        description.setManaged(description.isVisible());
        deleteButton.setOnAction(actionEvent -> deleteAction.run());
    }
}
