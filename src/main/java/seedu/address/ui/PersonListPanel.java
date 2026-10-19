package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a person list panel whose cards invoke the supplied deletion action.
     */
    public PersonListPanel(ObservableList<Person> personList, BiConsumer<Person, Integer> deleteAction) {
        super(FXML);
        requireNonNull(deleteAction);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell(deleteAction));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        private final BiConsumer<Person, Integer> deleteAction;

        PersonListViewCell(BiConsumer<Person, Integer> deleteAction) {
            this.deleteAction = deleteAction;
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                int displayedIndex = getIndex() + 1;
                Runnable onDelete = () -> deleteAction.accept(person, displayedIndex);
                PersonCard personCard = new PersonCard(person, displayedIndex, onDelete);
                setGraphic(personCard.getRoot());
            }
        }
    }

}
