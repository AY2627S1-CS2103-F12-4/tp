package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

/** Contains the primary actions displayed beside the list tabs. */
public class ActionBar extends UiPart<Region> {
    private static final String FXML = "ActionBar.fxml";

    @FXML
    private Button addPersonButton;

    @FXML
    private Button addEventButton;

    @FXML
    private Button clearSearchButton;

    /** Creates an action bar that delegates button presses to the supplied actions. */
    public ActionBar(Runnable addPersonAction, Runnable addEventAction, Runnable clearSearchAction) {
        super(FXML);
        requireNonNull(addPersonAction);
        requireNonNull(addEventAction);
        requireNonNull(clearSearchAction);
        addPersonButton.setOnAction(event -> addPersonAction.run());
        addEventButton.setOnAction(event -> addEventAction.run());
        clearSearchButton.setOnAction(event -> clearSearchAction.run());
    }

    /** Shows or hides the clear-search action. */
    void setClearSearchVisible(boolean isVisible) {
        clearSearchButton.setManaged(isVisible);
        clearSearchButton.setVisible(isVisible);
    }
}
