package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

public class EntryCardActionButtonTest {

    @Test
    public void eventCard_archiveState_displaysSuffixWithoutChangingStoredName() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard archived = new EventCard(event.withArchived(true), 1, () -> { }, () -> { });
            Label archivedName = (Label) archived.getRoot().lookup("#name");
            assertEquals("Workshop <archived>", archivedName.getText());
            EventCard active = new EventCard(event, 1, () -> { }, () -> { });
            assertEquals("Workshop", ((Label) active.getRoot().lookup("#name")).getText());
            assertEquals("Workshop", event.withArchived(true).getName());
        });
    }

    @Test
    public void personCard_deleteButton_runsConfiguredAction() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger deleteActions = new AtomicInteger();
            PersonCard card = new PersonCard(ALICE, 1, deleteActions::incrementAndGet);
            Button deleteButton = (Button) card.getRoot().lookup("#deleteButton");

            HBox cardPane = (HBox) card.getRoot();
            assertSame(deleteButton, cardPane.getChildren().get(cardPane.getChildren().size() - 1));
            assertRightGap(cardPane, deleteButton);
            deleteButton.fire();

            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void eventCard_deleteButton_runsConfiguredAction() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger deleteActions = new AtomicInteger();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard card = new EventCard(event, 1, () -> { }, deleteActions::incrementAndGet);
            Button deleteButton = (Button) card.getRoot().lookup("#deleteButton");

            HBox cardPane = (HBox) card.getRoot();
            assertSame(deleteButton, cardPane.getChildren().get(cardPane.getChildren().size() - 1));
            assertRightGap(cardPane, deleteButton);
            deleteButton.fire();

            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void eventCard_addParticipantButton_runsConfiguredAction() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger addParticipantActions = new AtomicInteger();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard card = new EventCard(event, 1, addParticipantActions::incrementAndGet, () -> { });
            Button addParticipantButton = (Button) card.getRoot().lookup("#addParticipantButton");

            assertEquals("Add participant", addParticipantButton.getText());
            addParticipantButton.fire();

            assertEquals(1, addParticipantActions.get());
        });
    }

    private void assertRightGap(HBox cardPane, Button deleteButton) {
        StackPane container = new StackPane(cardPane);
        new Scene(container, 400, 150);
        container.applyCss();
        container.layout();

        double rightGap = cardPane.getWidth() - deleteButton.getBoundsInParent().getMaxX();
        assertTrue(rightGap >= 16, "Delete buttons should have space before the scrollbar");
    }
}
