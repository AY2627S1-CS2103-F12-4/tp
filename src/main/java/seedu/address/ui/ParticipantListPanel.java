package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;

/**
 * Displays live participant search results and their selected event and search phrase.
 */
public class ParticipantListPanel extends UiPart<Region> {
    private static final String FXML = "ParticipantListPanel.fxml";

    @FXML
    private Label eventHeading;
    @FXML
    private Label searchHeading;
    @FXML
    private ListView<Participation> participantListView;

    /**
     * Creates a panel bound to the supplied observable search results.
     */
    public ParticipantListPanel(ObservableList<Participation> participants) {
        super(FXML);
        requireAllNonNull(participants);
        participantListView.setItems(participants);
        Label emptyMessage = new Label("No matching participants.");
        emptyMessage.getStyleClass().add("label-bright");
        participantListView.setPlaceholder(emptyMessage);
        participantListView.setCellFactory(listView -> new ParticipantListViewCell());
    }

    /**
     * Updates the heading to identify the event and name phrase associated with these results.
     */
    public void setSearchContext(Event event, String keyword) {
        requireAllNonNull(event, keyword);
        eventHeading.setText(event.getName() + " (" + event.getId() + ")");
        searchHeading.setText("Matching \"" + keyword + "\"");
    }

    /**
     * Renders a participant and clears recycled cells when they become empty.
     */
    private class ParticipantListViewCell extends ListCell<Participation> {
        @Override
        protected void updateItem(Participation participation, boolean empty) {
            super.updateItem(participation, empty);
            setText(null);
            setGraphic(empty || participation == null ? null : new ParticipantCard(participation).getRoot());
        }
    }
}
