package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import seedu.address.commons.core.LogsCenter;
import seedu.address.storage.ApplicationInstanceLock;

/**
 * The main entry point to the application.
 *
 * This is a workaround for the following error when MainApp is made the
 * entry point of the application:
 *
 *     Error: JavaFX runtime components are missing, and are required to run this application
 *
 * The reason is that MainApp extends Application. In that case, the
 * LauncherHelper will check for the javafx.graphics module to be present
 * as a named module. We don't use JavaFX via the module system so it can't
 * find the javafx.graphics module, and so the launch is aborted.
 *
 * By having a separate main class (Main) that doesn't extend Application
 * to be the entry point of the application, we avoid this issue.
 */
public class Main {
    private static Logger logger = LogsCenter.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            launchIfAvailable(MainApp.DATA_DIRECTORY, () -> {
                logger.warning("The warnings about a 'restricted method in java.lang.System' "
                        + "and 'enabling native access' appearing below (if any) can be ignored.");
                Application.launch(MainApp.class, args);
            });
        } catch (IOException e) {
            logger.severe("Could not access the application lock for data folder "
                    + MainApp.DATA_DIRECTORY.toAbsolutePath().normalize() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the blocking application launcher only while this process owns the data directory.
     * Returns false if another instance already owns it.
     */
    static boolean launchIfAvailable(Path dataDirectory, Runnable launchApplication) throws IOException {
        Optional<ApplicationInstanceLock> instanceLock = ApplicationInstanceLock.tryAcquire(dataDirectory);
        if (instanceLock.isEmpty()) {
            logger.warning("AttendPlusPlus is already running for this data folder: "
                    + dataDirectory.toAbsolutePath().normalize() + ". Close the other copy before trying again.");
            return false;
        }

        try (ApplicationInstanceLock lock = instanceLock.orElseThrow()) {
            launchApplication.run();
        }
        return true;
    }
}
