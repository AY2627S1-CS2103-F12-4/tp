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

    @FXML
    private Label eventHeading;

    @FXML
    private Label searchHeading;

    @FXML
    private ListView<Participation> participantListView;

    /**
     * Creates a participant panel bound initially to participant search
     * results.
     */
    public ParticipantListPanel(
            ObservableList<Participation> participantSearchResults) {
        super(FXML);

        requireAllNonNull(participantSearchResults);

        this.participantSearchResults = participantSearchResults;
        participantListView.setItems(participantSearchResults);

        Label emptyMessage =
                new Label("Select an event to view its participants.");
        emptyMessage.getStyleClass().add("label-bright");
        participantListView.setPlaceholder(emptyMessage);

        participantListView.setCellFactory(
                listView -> new ParticipantListViewCell());
    }

    /**
     * Displays all participants belonging to the selected event.
     */
    public void showParticipantsForEvent(
            Event event,
            Collection<Participation> participants) {
        requireAllNonNull(event, participants);

        ObservableList<Participation> displayedParticipants =
                FXCollections.observableArrayList(
                        participants.stream()
                                .sorted(Comparator.comparing(
                                        participation -> participation
                                                .getPerson()
                                                .getId()
                                                .toString()))
                                .toList());

        participantListView.setItems(displayedParticipants);

        eventHeading.setText(
                event.getName() + " (" + event.getId() + ")");
        searchHeading.setText("Participants");

        Label emptyMessage =
                new Label("No participants found for this event.");
        emptyMessage.getStyleClass().add("label-bright");
        participantListView.setPlaceholder(emptyMessage);
    }

    /**
     * Displays participant name-search results.
     */
    public void setSearchContext(Event event, String keyword) {
        requireAllNonNull(event, keyword);

        participantListView.setItems(participantSearchResults);

        eventHeading.setText(
                event.getName() + " (" + event.getId() + ")");
        searchHeading.setText("Matching \"" + keyword + "\"");

        Label emptyMessage = new Label("No matching participants.");
        emptyMessage.getStyleClass().add("label-bright");
        participantListView.setPlaceholder(emptyMessage);
    }

    /**
     * Restores the instructions shown when no event has been selected.
     */
    public void clearSearchContext() {
        eventHeading.setText("Participants");
        searchHeading.setText("Select an event to view its participants.");
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