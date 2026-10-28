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
    private Label archivedStatus;
    @FXML
    private Label eventId;
    @FXML
    private Label capacity;
    @FXML
    private Label description;
    @FXML
    private Button addParticipantButton;
    @FXML
    private Button editButton;
    @FXML
    private Button archiveButton;
    @FXML
    private Button deleteButton;

    /**
     * Creates an event card with its displayed index and entry actions.
     */
    public EventCard(Event event, int displayedIndex, Runnable addParticipantAction, Runnable deleteAction) {
        this(event, displayedIndex, addParticipantAction, () -> { }, () -> { }, deleteAction);
    }

    /**
     * Creates an event card with its displayed index, participant, edit and deletion actions.
     */
    public EventCard(Event event, int displayedIndex, Runnable addParticipantAction,
            Runnable editAction, Runnable deleteAction) {
        this(event, displayedIndex, addParticipantAction, editAction, () -> { }, deleteAction);
    }

    /**
     * Creates an event card with all event actions.
     */
    public EventCard(Event event, int displayedIndex, Runnable addParticipantAction,
            Runnable editAction, Runnable archiveAction, Runnable deleteAction) {
        super(FXML);
        requireNonNull(addParticipantAction);
        requireNonNull(editAction);
        requireNonNull(archiveAction);
        requireNonNull(deleteAction);
        index.setText(displayedIndex + ". ");
        name.setText(event.getName());
        archivedStatus.setVisible(event.isArchived());
        archivedStatus.setManaged(event.isArchived());
        eventId.setText("Event ID: " + event.getId());
        capacity.setText("Capacity: " + event.getCapacity());
        description.setText(event.getDescription());
        description.setVisible(!event.getDescription().isBlank());
        description.setManaged(description.isVisible());
        addParticipantButton.setOnAction(actionEvent -> addParticipantAction.run());
        editButton.setOnAction(actionEvent -> editAction.run());
        archiveButton.setText(event.isArchived() ? "Unarchive" : "Archive");
        archiveButton.setOnAction(actionEvent -> archiveAction.run());
        deleteButton.setOnAction(actionEvent -> deleteAction.run());
    }
}
