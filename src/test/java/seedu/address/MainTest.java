package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.storage.ApplicationInstanceLock;
import seedu.address.testutil.InstanceLockProcess;

public class MainTest {
    @TempDir
    public Path testFolder;

    @Test
    public void launchIfAvailable_freeDirectory_holdsLockThroughoutLaunchAndReleasesIt() throws Exception {
        AtomicBoolean launched = new AtomicBoolean();
        assertTrue(Main.launchIfAvailable(testFolder, () -> {
            launched.set(true);
            Path statusFile = testFolder.resolve("contender.status");
            try {
                Process contender = InstanceLockProcess.start(testFolder, statusFile);
                try {
                    assertEquals("BUSY", InstanceLockProcess.awaitStatus(contender, statusFile));
                } finally {
                    InstanceLockProcess.stop(contender);
                }
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }));
        assertTrue(launched.get());
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(testFolder).orElseThrow()) {
            assertTrue(Files.exists(testFolder.resolve(".attendplusplus.lock")));
        }
    }

    @Test
    public void launchIfAvailable_launchFails_releasesLockAndPropagatesFailure() throws Exception {
        RuntimeException failure = new RuntimeException("Startup failed");
        assertSame(failure, assertThrows(RuntimeException.class, () -> Main.launchIfAvailable(testFolder, () -> {
            throw failure;
        })));
        assertTrue(Main.launchIfAvailable(testFolder, () -> {}));
    }

    @Test
    public void launchIfAvailable_otherProcessOwnsDirectory_skipsLaunchAndPreservesSavedFiles() throws Exception {
        Path dataDirectory = Files.createDirectory(testFolder.resolve("data"));
        List<Path> savedFiles = List.of(dataDirectory.resolve("addressbook.json"),
                dataDirectory.resolve("events.json"), dataDirectory.resolve("participations.json"),
                testFolder.resolve("preferences.json"));
        for (Path savedFile : savedFiles) {
            Files.writeString(savedFile, "Preserve " + savedFile.getFileName());
        }
        Path statusFile = testFolder.resolve("owner.status");
        Process owner = InstanceLockProcess.start(dataDirectory, statusFile);
        try {
            assertEquals("LOCKED", InstanceLockProcess.awaitStatus(owner, statusFile));
            assertFalse(Main.launchIfAvailable(dataDirectory, () -> fail("A second instance must not launch")));
            for (Path savedFile : savedFiles) {
                assertEquals("Preserve " + savedFile.getFileName(), Files.readString(savedFile));
            }
        } finally {
            InstanceLockProcess.stop(owner);
        }
    }

    @Test
    public void launchIfAvailable_lockFileInaccessible_skipsLaunchAndReportsIoFailure() throws Exception {
        Files.createDirectory(testFolder.resolve(".attendplusplus.lock"));
        assertThrows(IOException.class, () -> {
            Main.launchIfAvailable(testFolder, () -> fail("An unsecured instance must not launch"));
        });
    }
}
