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
     * Creates an event list panel that reports selection changes.
     *
     * @param events events to display
     * @param onEventSelected action performed when the selection changes
     */
    public EventListPanel(
            ObservableList<Event> events,
            Consumer<Event> onEventSelected) {
        this(
                events,
                onEventSelected,
                event -> {
                },
                event -> {
                });
    }

    /**
     * Creates an event list panel whose cards invoke the supplied actions.
     *
     * <p>This constructor is retained for compatibility with existing callers.
     *
     * @param events events to display
     * @param addParticipantAction action performed when adding a participant
     * @param deleteAction action performed when deleting an event
     */
    public EventListPanel(
            ObservableList<Event> events,
            Consumer<Event> addParticipantAction,
            Consumer<Event> deleteAction) {
        this(
                events,
                event -> {
                },
                addParticipantAction,
                deleteAction);
    }

    /**
     * Creates an event list panel with selection and card actions.
     *
     * @param events events to display
     * @param onEventSelected action performed when the selection changes
     * @param addParticipantAction action performed when adding a participant
     * @param deleteAction action performed when deleting an event
     */
    public EventListPanel(
            ObservableList<Event> events,
            Consumer<Event> onEventSelected,
            Consumer<Event> addParticipantAction,
            Consumer<Event> deleteAction) {
        super(FXML);

        requireNonNull(events);
        requireNonNull(onEventSelected);
        requireNonNull(addParticipantAction);
        requireNonNull(deleteAction);

        eventListView.setItems(events);

        Label emptyMessage = new Label("No events to display.");
        emptyMessage.getStyleClass().add("label-bright");
        eventListView.setPlaceholder(emptyMessage);

        eventListView.setCellFactory(
                listView -> new EventListViewCell(
                        addParticipantAction,
                        deleteAction));

        eventListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, previousEvent, selectedEvent) ->
                        onEventSelected.accept(selectedEvent));
    }

    /**
     * Clears the currently selected event.
     */
    public void clearSelection() {
        eventListView.getSelectionModel().clearSelection();
    }

    /**
     * Renders an event and clears recycled cells when they become empty.
     */
    private class EventListViewCell extends ListCell<Event> {

        private final Consumer<Event> addParticipantAction;
        private final Consumer<Event> deleteAction;

        EventListViewCell(
                Consumer<Event> addParticipantAction,
                Consumer<Event> deleteAction) {
            this.addParticipantAction = addParticipantAction;
            this.deleteAction = deleteAction;
        }

        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);
            setText(null);

            if (empty || event == null) {
                setGraphic(null);
                return;
            }

            Runnable onAddParticipant =
                    () -> addParticipantAction.accept(event);
            Runnable onDelete =
                    () -> deleteAction.accept(event);

            EventCard eventCard = new EventCard(
                    event,
                    getIndex() + 1,
                    onAddParticipant,
                    onDelete);

            setGraphic(eventCard.getRoot());
        }
    }
}