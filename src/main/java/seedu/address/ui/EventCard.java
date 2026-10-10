package seedu.address.ui;

import javafx.fxml.FXML;
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

    /**
     * Creates a card for the supplied event with a one-based displayed index.
     */
    public EventCard(Event event, int displayedIndex) {
        super(FXML);
        index.setText(displayedIndex + ". ");
        name.setText(event.getName());
        eventId.setText("Event ID: " + event.getId());
        capacity.setText("Capacity: " + event.getCapacity());
        description.setText(event.getDescription());
        description.setVisible(!event.getDescription().isBlank());
        description.setManaged(description.isVisible());
    }
}
