package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.event.Event;

/**
 * Displays a live list of events using numbered event cards.
 */
public class EventListPanel extends UiPart<Region> {
    private static final String FXML = "EventListPanel.fxml";

    @FXML
    private ListView<Event> eventListView;

    /**
     * Creates a panel bound to the supplied observable event list.
     */
    public EventListPanel(ObservableList<Event> events) {
        super(FXML);
        eventListView.setItems(events);
        Label emptyMessage = new Label("No events to display.");
        emptyMessage.getStyleClass().add("label-bright");
        eventListView.setPlaceholder(emptyMessage);
        eventListView.setCellFactory(listView -> new EventListViewCell());
    }

    /**
     * Renders an event and clears recycled cells when they become empty.
     */
    private class EventListViewCell extends ListCell<Event> {
        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);
            setText(null);
            setGraphic(empty || event == null ? null : new EventCard(event, getIndex() + 1).getRoot());
        }
    }
}
