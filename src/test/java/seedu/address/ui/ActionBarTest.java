package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            ActionBar actionBar = new ActionBar(personActions::incrementAndGet, eventActions::incrementAndGet);
            Button addPersonButton = (Button) actionBar.getRoot().lookup("#addPersonButton");
            Button addEventButton = (Button) actionBar.getRoot().lookup("#addEventButton");

            addPersonButton.fire();
            addEventButton.fire();

            assertEquals(1, personActions.get());
            assertEquals(1, eventActions.get());
        });
    }
}
