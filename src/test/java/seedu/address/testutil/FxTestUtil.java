package seedu.address.testutil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Runs UI assertions on the JavaFX application thread without opening application windows.
 */
public class FxTestUtil {
    private static final CompletableFuture<Void> TOOLKIT_READY = initializeToolkit();

    private FxTestUtil() {}

    private static CompletableFuture<Void> initializeToolkit() {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.complete(null);
        });
        return ready;
    }

    /**
     * Runs an action on the JavaFX application thread and propagates failures to the test runner.
     */
    public static void runOnFxThread(ThrowingRunnable action) throws Exception {
        TOOLKIT_READY.get(10, TimeUnit.SECONDS);
        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    /**
     * Represents a UI test action that may throw a checked exception.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
