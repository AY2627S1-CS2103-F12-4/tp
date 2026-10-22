package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;

public class ActionBarTest {

    @Test
    public void buttons_fired_runConfiguredActions() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger personActions = new AtomicInteger();
            AtomicInteger eventActions = new AtomicInteger();
            AtomicInteger clearSearchActions = new AtomicInteger();
            ActionBar actionBar = new ActionBar(
                    personActions::incrementAndGet,
                    eventActions::incrementAndGet,
                    clearSearchActions::incrementAndGet);
            Button addPersonButton = (Button) actionBar.getRoot().lookup("#addPersonButton");
            Button addEventButton = (Button) actionBar.getRoot().lookup("#addEventButton");
            Button clearSearchButton = (Button) actionBar.getRoot().lookup("#clearSearchButton");

            addPersonButton.fire();
            addEventButton.fire();
            assertFalse(clearSearchButton.isVisible());
            actionBar.setClearSearchVisible(true);
            clearSearchButton.fire();

            assertEquals(1, personActions.get());
            assertEquals(1, eventActions.get());
            assertEquals(1, clearSearchActions.get());
            assertTrue(clearSearchButton.isManaged());
            assertTrue(clearSearchButton.isVisible());
        });
    }
}
