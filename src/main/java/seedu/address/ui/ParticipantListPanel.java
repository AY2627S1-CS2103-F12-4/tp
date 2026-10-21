package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.Comparator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;

/**
 * Displays participants belonging to the selected event or participant
 * search results.
 */
public class ParticipantListPanel extends UiPart<Region> {

    private static final String FXML = "ParticipantListPanel.fxml";

    private final ObservableList<Participation> participantSearchResults;
    private final ObservableList<Participation> eventParticipants =
            FXCollections.observableArrayList();

    @FXML
    private Label eventHeading;

    @FXML
    private Label searchHeading;

    @FXML
    private ListView<Participation> participantListView;

    /**
     * Creates a participant panel bound initially to participant search
     * results.
     *
     * @param participantSearchResults observable participant search results
     */
    public ParticipantListPanel(
            ObservableList<Participation> participantSearchResults) {
        super(FXML);

        requireAllNonNull(participantSearchResults);

        this.participantSearchResults = participantSearchResults;
        participantListView.setItems(participantSearchResults);
        setPlaceholder("Select an event to view its participants.");

        participantListView.setCellFactory(
                listView -> new ParticipantListViewCell());
    }

    /**
     * Displays all participants belonging to the selected event.
     *
     * @param event selected event
     * @param participants participants belonging to the event
     */
    public void showParticipantsForEvent(
            Event event,
            Collection<Participation> participants) {
        requireAllNonNull(event, participants);

        eventParticipants.setAll(
                participants.stream()
                        .sorted(Comparator.comparing(
                                participation -> participation
                                        .getPerson()
                                        .getId()
                                        .toString()))
                        .toList());

        participantListView.setItems(eventParticipants);

        eventHeading.setText(
                event.getDisplayName() + " (" + event.getId() + ")");
        searchHeading.setText("Participants");

        setPlaceholder("No participants found for this event.");
    }

    /**
     * Displays participant search results.
     *
     * @param event event associated with the search
     * @param keyword original search keyword
     */
    public void setSearchContext(Event event, String keyword) {
        requireAllNonNull(event, keyword);

        participantListView.setItems(participantSearchResults);

        eventHeading.setText(
                event.getDisplayName() + " (" + event.getId() + ")");
        searchHeading.setText("Matching \"" + keyword + "\"");

        setPlaceholder("No matching participants.");
    }

    /** Displays global search results and their query across nonarchived events. */
    public void setSearchContext(String keyword) {
        requireAllNonNull(keyword);
        participantListView.setItems(participantSearchResults);
        eventHeading.setText("All nonarchived events");
        searchHeading.setText("Matching \"" + keyword + "\"");
        setPlaceholder("No matching participants.");
    }

    /**
     * Clears the displayed participants and restores the instructions shown
     * when no event is selected.
     */
    public void clearSearchContext() {
        eventParticipants.clear();
        participantListView.setItems(eventParticipants);

        eventHeading.setText("Participants");
        searchHeading.setText(
                "Select an event to view its participants.");

        setPlaceholder("Select an event to view its participants.");
    }

    private void setPlaceholder(String message) {
        Label emptyMessage = new Label(message);
        emptyMessage.getStyleClass().add("label-bright");
        participantListView.setPlaceholder(emptyMessage);
    }

    /**
     * Renders participant cards.
     */
    private class ParticipantListViewCell
            extends ListCell<Participation> {

        @Override
        protected void updateItem(
                Participation participation,
                boolean empty) {
            super.updateItem(participation, empty);
            setText(null);

            setGraphic(empty || participation == null
                    ? null
                    : new ParticipantCard(participation).getRoot());
        }
    }
}
