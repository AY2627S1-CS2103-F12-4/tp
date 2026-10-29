package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Holds exclusive ownership of a data directory for one running application instance.
 */
public final class ApplicationInstanceLock implements AutoCloseable {
    private static final String LOCK_FILE_NAME = ".attendplusplus.lock";

    private final FileChannel channel;
    private final FileLock lock;

    private ApplicationInstanceLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Tries to acquire ownership without waiting for another instance to exit.
     * Returns an empty result if the directory is already in use.
     *
     * @throws IOException if the directory or lock file cannot be accessed, or locking fails.
     */
    public static Optional<ApplicationInstanceLock> tryAcquire(Path dataDirectory) throws IOException {
        requireNonNull(dataDirectory);
        Files.createDirectories(dataDirectory);
        Path lockPath = dataDirectory.toRealPath().resolve(LOCK_FILE_NAME);
        FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        boolean acquired = false;
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                return Optional.empty();
            }
            acquired = true;
            return Optional.of(new ApplicationInstanceLock(channel, lock));
        } catch (OverlappingFileLockException e) {
            return Optional.empty();
        } finally {
            if (!acquired) {
                channel.close();
            }
        }
    }

    /**
     * Releases ownership and closes the channel. The lock file is retained for future launches.
     */
    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            channel.close();
        }
    }
}
