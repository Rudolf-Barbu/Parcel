package org.bsoftware.parcel.domain.runnables;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ConnectionUtility;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataProcessingRunnable is a class that represent worker, which is used for data processing
 *
 * @author Rudolf Barbu
 * @version 1.0.9
 */
@RequiredArgsConstructor
public class DataLoadingRunnable implements Runnable
{
    /**
     * Defines text file extension pattern
     */
    private static final String TEXT_FILE_EXTENSION_PATTERN = "text/plain";

    /**
     * Defines source and proxy line delimiter
     */
    private static final char DELIMITER = ':';

    /**
     * Defines email domain symbol
     */
    private static final char DOMAIN = '@';

    /**
     * Defines max lines allowed to contain in a processing file
     */
    private static final int MAX_LINES_ALLOWED = 16_777_216;

    /**
     * Defines regular expression for proxy's IP address
     */
    private static final String IP_ADDRESS_REGULAR_EXPRESSION = "(\\d{1,3}\\.){3}\\d{1,3}";

    /**
     * Target file, for processing
     */
    private final File file;

    /**
     * Data-type for validation
     */
    private final DataType dataType;

    /**
     * Callback interface, which is used to deliver messages to controller
     */
    private final DataLoadingCallback dataLoadingCallback;

    /**
     * Main method of processing algorithm
     */
    @Override
    public void run()
    {
        try
        {
            if (!new Tika().detect(file).equals(TEXT_FILE_EXTENSION_PATTERN))
            {
                dataLoadingCallback.handleDataLoadingMessage(LogLevel.ERROR, "Only text files allowed");
                return;
            }

            final Collection<String> buffer = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream().filter(line -> singleEntryCheck(line, DELIMITER)).collect(Collectors.toList());

            if (buffer.size() > MAX_LINES_ALLOWED)
            {
                dataLoadingCallback.handleDataLoadingMessage(LogLevel.ERROR, "Exceeded line count limit");
                return;
            }

            dataLoadingCallback.handleLoadedData((dataType == DataType.SOURCE) ? loadSources(buffer) : loadProxies(buffer), dataType);
        }
        catch (final IOException ioException)
        {
            dataLoadingCallback.handleDataLoadingMessage(LogLevel.ERROR, String.format("I/O exception occurred, message: %s", ioException.getMessage()));
        }
    }

    /**
     * Checks, if presented symbol entries the line, but only one time
     *
     * @param line particular line, to be checked
     * @param indexSymbol symbol to find
     * @return true, if line passes the check
     */
    private boolean singleEntryCheck(final String line, final char indexSymbol)
    {
        return (!line.isEmpty() && ((line.indexOf(indexSymbol) != -1) && (line.indexOf(indexSymbol) == line.lastIndexOf(indexSymbol))));
    }

    /**
     * Source validation method
     *
     * @param unprocessedLines list, which contains unprocessed lines
     * @return unique set of loaded sources
     */
    private Collection<Source> loadSources(final Collection<String> unprocessedLines)
    {
        final Set<Source> buffer = new HashSet<>();

        for (final String source : unprocessedLines)
        {
            final String credential = source.substring(0, source.indexOf(DELIMITER));

            if (singleEntryCheck(credential, DOMAIN) && ConnectionUtility.isConnectionSupported(credential))
            {
                final String password = source.substring(source.indexOf(DELIMITER) + 1);
                buffer.add(new Source(credential, password));
            }
        }

        return buffer;
    }

    /**
     * Proxy validation method
     *
     * @param unprocessedLines list, which contains unprocessed lines
     * @return unique set of loaded proxies
     */
    private Collection<Proxy> loadProxies(final Collection<String> unprocessedLines)
    {
        final Set<Proxy> buffer = new HashSet<>();

        for (final String proxy : unprocessedLines)
        {
            final String ipAddress = proxy.substring(0, proxy.indexOf(DELIMITER));
            final int port = Integer.parseInt(proxy.substring(proxy.indexOf(DELIMITER) + 1));

            if (ipAddress.matches(IP_ADDRESS_REGULAR_EXPRESSION) && ((port > 80) && (port < 65_535)))
            {
                buffer.add(new Proxy(ipAddress, port));
            }
        }

        return buffer;
    }
}