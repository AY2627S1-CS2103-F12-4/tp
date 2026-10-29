package seedu.address.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;

/**
 * Provides common search window interactions and lookups for use on the JavaFX application thread.
 * Control lookups preserve the selected tab and event.
 */
final class SearchUiTestUtil {
    private SearchUiTestUtil() {
    }

    /**
     * Enters a command through the window's command box.
     */
    static void enterCommand(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    /**
     * Initializes the Events tab's controls during window setup and restores the selected tab.
     */
    static void prepareEventTab(MainWindow window) {
        TabPane tabs = getTabs(window);
        Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
        tabs.getSelectionModel().select(1);
        try {
            window.getPrimaryStage().getScene().getRoot().applyCss();
        } finally {
            tabs.getSelectionModel().select(selectedTab);
        }
    }

    static TabPane getTabs(MainWindow window) {
        return (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
    }

    static String getSelectedTabName(MainWindow window) {
        return getTabs(window).getSelectionModel().getSelectedItem().getText();
    }

    @SuppressWarnings("unchecked")
    static ListView<Event> getEvents(MainWindow window) {
        return (ListView<Event>) getTabs(window).getTabs().get(1).getContent().lookup("#eventListView");
    }

    @SuppressWarnings("unchecked")
    static ListView<Participation> getParticipants(MainWindow window) {
        return (ListView<Participation>) window.getParticipantListPanel().getRoot().lookup("#participantListView");
    }

    static String getHeading(MainWindow window, String id) {
        return ((Label) window.getParticipantListPanel().getRoot().lookup("#" + id)).getText();
    }

    static String getFeedback(MainWindow window) {
        return ((TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay")).getText();
    }
}
