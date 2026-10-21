package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;

public class ListPanelActionTest {

    private static final Event EVENT =
            new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

    @Test
    public void personPanel_actionButtons_forwardPersonAndDisplayedIndex()
            throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Person> editedPerson = new AtomicReference<>();
            AtomicInteger editedIndex = new AtomicInteger();
            AtomicReference<Person> deletedPerson =
                    new AtomicReference<>();
            AtomicInteger deletedIndex =
                    new AtomicInteger();

            BiConsumer<Person, Integer> deleteAction = (person, index) -> {
                deletedPerson.set(person);
                deletedIndex.set(index);
            };
            BiConsumer<Person, Integer> editAction = (person, index) -> {
                editedPerson.set(person);
                editedIndex.set(index);
            };

            PersonListPanel panel = new PersonListPanel(
                    FXCollections.observableArrayList(ALICE),
                    editAction,
                    deleteAction);

            ListView<Person> list = personListView(panel);
            ListCell<Person> cell =
                    list.getCellFactory().call(list);

            cell.updateListView(list);
            cell.updateIndex(0);

            Button editButton =
                    (Button) cell.getGraphic()
                            .lookup("#editButton");
            Button deleteButton =
                    (Button) cell.getGraphic()
                            .lookup("#deleteButton");
            editButton.fire();
            deleteButton.fire();

            assertSame(ALICE, editedPerson.get());
            assertEquals(1, editedIndex.get());
            assertSame(ALICE, deletedPerson.get());
            assertEquals(1, deletedIndex.get());

            cell.updateIndex(1);

            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void eventPanel_actionButtons_forwardEvent() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Event> participantEvent =
                    new AtomicReference<>();
            AtomicReference<Event> deletedEvent =
                    new AtomicReference<>();

            EventListPanel panel = new EventListPanel(
                    FXCollections.observableArrayList(EVENT),
                    participantEvent::set,
                    deletedEvent::set);

            ListView<Event> list = eventListView(panel);
            ListCell<Event> cell =
                    list.getCellFactory().call(list);

            cell.updateListView(list);
            cell.updateIndex(0);

            assertNotNull(cell.getGraphic());

            Button addParticipantButton =
                    (Button) cell.getGraphic()
                            .lookup("#addParticipantButton");
            Button deleteButton =
                    (Button) cell.getGraphic()
                            .lookup("#deleteButton");

            addParticipantButton.fire();
            deleteButton.fire();

            assertSame(EVENT, participantEvent.get());
            assertSame(EVENT, deletedEvent.get());

            cell.updateIndex(1);

            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void eventPanel_selectionChange_forwardsSelectedEvent()
            throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Event> selectedEvent =
                    new AtomicReference<>();

            EventListPanel panel = new EventListPanel(
                    FXCollections.observableArrayList(EVENT),
                    selectedEvent::set);

            ListView<Event> list = eventListView(panel);

            list.getSelectionModel().select(EVENT);

            assertSame(EVENT, selectedEvent.get());

            panel.clearSelection();

            assertNull(selectedEvent.get());
            assertNull(
                    list.getSelectionModel().getSelectedItem());
        });
    }

    @Test
    public void eventPanel_allActions_forwardCorrectEvent()
            throws Exception {
        runOnFxThread(() -> {
            AtomicReference<Event> selectedEvent =
                    new AtomicReference<>();
            AtomicReference<Event> participantEvent =
                    new AtomicReference<>();
            AtomicReference<Event> editedEvent =
                    new AtomicReference<>();
            AtomicReference<Event> deletedEvent =
                    new AtomicReference<>();

            EventListPanel panel = new EventListPanel(
                    FXCollections.observableArrayList(EVENT),
                    selectedEvent::set,
                    participantEvent::set,
                    editedEvent::set,
                    deletedEvent::set);

            ListView<Event> list = eventListView(panel);
            ListCell<Event> cell =
                    list.getCellFactory().call(list);

            list.getSelectionModel().select(EVENT);
            cell.updateListView(list);
            cell.updateIndex(0);

            Button addParticipantButton =
                    (Button) cell.getGraphic()
                            .lookup("#addParticipantButton");
            Button editButton =
                    (Button) cell.getGraphic()
                            .lookup("#editButton");
            Button deleteButton =
                    (Button) cell.getGraphic()
                            .lookup("#deleteButton");

            addParticipantButton.fire();
            editButton.fire();
            deleteButton.fire();

            assertSame(EVENT, selectedEvent.get());
            assertSame(EVENT, participantEvent.get());
            assertSame(EVENT, editedEvent.get());
            assertSame(EVENT, deletedEvent.get());
        });
    }

    @Test
    public void eventPanel_nullArguments_rejectInput()
            throws Exception {
        runOnFxThread(() -> {
            ObservableList<Event> events =
                    FXCollections.observableArrayList(EVENT);
            AtomicReference<Event> action =
                    new AtomicReference<>();

            assertThrows(
                    NullPointerException.class, () ->
                            new EventListPanel(
                                    null,
                                    action::set,
                                    action::set,
                                    action::set));

            assertThrows(
                    NullPointerException.class, () ->
                            new EventListPanel(
                                    events,
                                    null,
                                    action::set,
                                    action::set));

            assertThrows(
                    NullPointerException.class, () ->
                            new EventListPanel(
                                    events,
                                    action::set,
                                    null,
                                    action::set));

            assertThrows(
                    NullPointerException.class, () ->
                            new EventListPanel(
                                    events,
                                    action::set,
                                    action::set,
                                    null));

            assertThrows(
                    NullPointerException.class, () ->
                            new EventListPanel(
                                    events,
                                    action::set,
                                    action::set,
                                    null,
                                    action::set));
        });
    }

    @SuppressWarnings("unchecked")
    private ListView<Person> personListView(
            PersonListPanel panel) {
        return (ListView<Person>) panel.getRoot()
                .lookup("#personListView");
    }

    @SuppressWarnings("unchecked")
    private ListView<Event> eventListView(
            EventListPanel panel) {
        return (ListView<Event>) panel.getRoot()
                .lookup("#eventListView");
    }
}
