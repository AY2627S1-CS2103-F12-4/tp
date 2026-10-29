package seedu.address.testutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import seedu.address.storage.ApplicationInstanceLock;

/**
 * A separate JVM that reports lock acquisition and holds ownership until its input closes.
 */
public final class InstanceLockProcess {
    private InstanceLockProcess() {}

    /** Starts a lock contender and redirects diagnostic output beside its status file. */
    public static Process start(Path dataDirectory, Path statusFile) throws Exception {
        String classPath = Path.of(InstanceLockProcess.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()) + File.pathSeparator
                + Path.of(ApplicationInstanceLock.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classPath, InstanceLockProcess.class.getName(), dataDirectory.toString(), statusFile.toString())
                .redirectErrorStream(true)
                .redirectOutput(statusFile.resolveSibling(statusFile.getFileName() + ".log").toFile())
                .start();
    }

    /** Waits for an explicit readiness signal, with a bounded timeout. */
    public static String awaitStatus(Process process, Path statusFile) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!Files.exists(statusFile)) {
            if (!process.isAlive() || System.nanoTime() >= deadline) {
                if (Files.exists(statusFile)) {
                    break;
                }
                Path logFile = statusFile.resolveSibling(statusFile.getFileName() + ".log");
                throw new AssertionError("Lock process did not report readiness: " + Files.readString(logFile));
            }
            Thread.sleep(20);
        }
        return Files.readString(statusFile);
    }

    /** Terminates and reaps a process, including when a test assertion fails. */
    public static void stop(Process process) throws InterruptedException {
        process.destroyForcibly();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Lock process did not terminate");
        }
    }

    /** Reports ownership atomically, then waits for the parent to request normal shutdown. */
    public static void main(String[] args) throws IOException {
        Path statusFile = Path.of(args[1]);
        Optional<ApplicationInstanceLock> instanceLock = ApplicationInstanceLock.tryAcquire(Path.of(args[0]));
        if (instanceLock.isEmpty()) {
            reportStatus(statusFile, "BUSY");
            return;
        }
        try (ApplicationInstanceLock lock = instanceLock.orElseThrow()) {
            reportStatus(statusFile, "LOCKED");
            System.in.read();
        }
    }

    private static void reportStatus(Path statusFile, String status) throws IOException {
        Path pendingFile = statusFile.resolveSibling(statusFile.getFileName() + ".pending");
        Files.writeString(pendingFile, status);
        Files.move(pendingFile, statusFile);
    }
}
