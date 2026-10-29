package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.testutil.InstanceLockProcess;

public class ApplicationInstanceLockTest {
    @TempDir
    public Path testFolder;

    @Test
    public void tryAcquire_missingDirectory_createsDirectoryAndReleasesOwnership() throws Exception {
        Path dataDirectory = testFolder.resolve("nested/data");
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(dataDirectory).orElseThrow()) {
            assertTrue(Files.exists(dataDirectory.resolve(".attendplusplus.lock")));
            assertFalse(ApplicationInstanceLock.tryAcquire(dataDirectory).isPresent());
        }
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(dataDirectory).orElseThrow()) {
            assertTrue(Files.exists(dataDirectory.resolve(".attendplusplus.lock")));
        }
    }

    @Test
    public void tryAcquire_existingLockFile_preservesContentsAndAllowsRestart() throws Exception {
        Path lockFile = testFolder.resolve(".attendplusplus.lock");
        Files.writeString(lockFile, "An existing file is not an active lock.");
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(testFolder).orElseThrow()) {
            assertTrue(Files.exists(lockFile));
        }
        assertEquals("An existing file is not an active lock.", Files.readString(lockFile));
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(testFolder).orElseThrow()) {
            assertTrue(Files.exists(lockFile));
        }
        assertEquals("An existing file is not an active lock.", Files.readString(lockFile));
    }

    @Test
    public void tryAcquire_invalidDirectoryOrLockFile_throwsIoException() throws Exception {
        Path regularFile = testFolder.resolve("not-a-directory");
        Files.writeString(regularFile, "existing content");
        assertThrows(IOException.class, () -> ApplicationInstanceLock.tryAcquire(regularFile));
        assertEquals("existing content", Files.readString(regularFile));

        Files.createDirectory(testFolder.resolve(".attendplusplus.lock"));
        assertThrows(IOException.class, () -> ApplicationInstanceLock.tryAcquire(testFolder));
    }

    @Test
    public void tryAcquire_otherProcessOwnsDirectory_rejectsUntilNormalShutdown() throws Exception {
        Path statusFile = testFolder.resolve("owner.status");
        Process owner = InstanceLockProcess.start(testFolder, statusFile);
        try {
            assertEquals("LOCKED", InstanceLockProcess.awaitStatus(owner, statusFile));
            assertTrue(ApplicationInstanceLock.tryAcquire(testFolder).isEmpty());
            assertTrue(ApplicationInstanceLock.tryAcquire(testFolder.resolve(".")).isEmpty());

            owner.getOutputStream().close();
            assertTrue(owner.waitFor(10, TimeUnit.SECONDS));
            assertEquals(0, owner.exitValue());
            try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(testFolder).orElseThrow()) {
                assertTrue(Files.exists(testFolder.resolve(".attendplusplus.lock")));
            }
        } finally {
            InstanceLockProcess.stop(owner);
        }
    }

    @Test
    public void tryAcquire_ownerTerminated_allowsRestart() throws Exception {
        Path statusFile = testFolder.resolve("owner.status");
        Process owner = InstanceLockProcess.start(testFolder, statusFile);
        try {
            assertEquals("LOCKED", InstanceLockProcess.awaitStatus(owner, statusFile));
        } finally {
            InstanceLockProcess.stop(owner);
        }
        try (ApplicationInstanceLock lock = ApplicationInstanceLock.tryAcquire(testFolder).orElseThrow()) {
            assertTrue(Files.exists(testFolder.resolve(".attendplusplus.lock")));
        }
    }

    @Test
    public void tryAcquire_differentDirectories_allowsIndependentProcesses() throws Exception {
        Path firstStatus = testFolder.resolve("first.status");
        Path secondStatus = testFolder.resolve("second.status");
        Process first = InstanceLockProcess.start(testFolder.resolve("first"), firstStatus);
        try {
            assertEquals("LOCKED", InstanceLockProcess.awaitStatus(first, firstStatus));
            Process second = InstanceLockProcess.start(testFolder.resolve("second"), secondStatus);
            try {
                assertEquals("LOCKED", InstanceLockProcess.awaitStatus(second, secondStatus));
            } finally {
                InstanceLockProcess.stop(second);
            }
        } finally {
            InstanceLockProcess.stop(first);
        }
    }

    @Test
    public void tryAcquire_concurrentProcesses_exactlyOneOwnsDirectory() throws Exception {
        Path firstStatus = testFolder.resolve("first.status");
        Path secondStatus = testFolder.resolve("second.status");
        Process first = InstanceLockProcess.start(testFolder, firstStatus);
        try {
            Process second = InstanceLockProcess.start(testFolder, secondStatus);
            try {
                String firstResult = InstanceLockProcess.awaitStatus(first, firstStatus);
                String secondResult = InstanceLockProcess.awaitStatus(second, secondStatus);
                assertEquals(Set.of("LOCKED", "BUSY"), Set.of(firstResult, secondResult));
            } finally {
                InstanceLockProcess.stop(second);
            }
        } finally {
            InstanceLockProcess.stop(first);
        }
    }
}
