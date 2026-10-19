package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

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
     * Creates an event list panel whose cards invoke the supplied deletion action.
     */
    public EventListPanel(ObservableList<Event> events, Consumer<Event> deleteAction) {
        super(FXML);
        requireNonNull(deleteAction);
        eventListView.setItems(events);
        Label emptyMessage = new Label("No events to display.");
        emptyMessage.getStyleClass().add("label-bright");
        eventListView.setPlaceholder(emptyMessage);
        eventListView.setCellFactory(listView -> new EventListViewCell(deleteAction));
    }

    /**
     * Renders an event and clears recycled cells when they become empty.
     */
    private class EventListViewCell extends ListCell<Event> {
        private final Consumer<Event> deleteAction;

        EventListViewCell(Consumer<Event> deleteAction) {
            this.deleteAction = deleteAction;
        }

        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);
            setText(null);
            if (empty || event == null) {
                setGraphic(null);
            } else {
                EventCard eventCard = new EventCard(event, getIndex() + 1, () -> deleteAction.accept(event));
                setGraphic(eventCard.getRoot());
            }
        }
    }
}
