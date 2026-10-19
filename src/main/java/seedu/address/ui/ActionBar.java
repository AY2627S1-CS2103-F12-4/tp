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

    /** Creates an action bar that delegates button presses to the supplied actions. */
    public ActionBar(Runnable addPersonAction, Runnable addEventAction) {
        super(FXML);
        requireNonNull(addPersonAction);
        requireNonNull(addEventAction);
        addPersonButton.setOnAction(event -> addPersonAction.run());
        addEventButton.setOnAction(event -> addEventAction.run());
    }
}
