package org.bsoftware.parcel.domain.runnables;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ConnectionUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataProcessingRunnable is a class that represent worker, which is used for data processing
 *
 * @author Rudolf Barbu
 * @version 1.0.7
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
                dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, "Only text files allowed");
                return;
            }

            final List<String> buffer = Files.readAllLines(file.toPath()).stream().filter(line -> singleEntryCheck(line, DELIMITER)).collect(Collectors.toList());

            if (buffer.size() > MAX_LINES_ALLOWED)
            {
                dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, "Exceeded line count limit");
                return;
            }

            dataLoadingCallback.handleLoadedData((dataType == DataType.SOURCE) ? loadSources(buffer) : loadProxies(buffer), dataType);
        }
        catch (final IOException ioException)
        {
            dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, String.format("IO exception occurred, clause: %s", ioException.getMessage()));
        }
    }

    /**
     * Checks, if presented symbol entries the line, but only one time
     *
     * @param line - particular line, to be checked
     * @param indexSymbol - symbol to find
     * @return true, if line passes the check
     */
    private boolean singleEntryCheck(final String line, final char indexSymbol)
    {
        return (!line.isEmpty() && ((line.indexOf(indexSymbol) != -1) && (line.indexOf(indexSymbol) == line.lastIndexOf(indexSymbol))));
    }

    /**
     * Source validation method
     *
     * @param buffer - list, which contains unprocessed lines
     * @return unique set of loaded sources
     */
    private Set<Source> loadSources(final List<String> buffer)
    {
        final Set<Source> resultSet = new HashSet<>();

        for (final String source : buffer)
        {
            final String credential = source.substring(0, source.indexOf(DELIMITER));

            if (singleEntryCheck(credential, DOMAIN) && ConnectionUtility.isConnectionSupported(credential))
            {
                final String password = source.substring(source.indexOf(DELIMITER) + 1);
                resultSet.add(new Source(credential, password));
            }
        }

        return resultSet;
    }

    /**
     * Proxy validation method
     *
     * @param buffer - list, which contains unprocessed lines
     * @return unique set of loaded proxies
     */
    private Set<Proxy> loadProxies(final List<String> buffer)
    {
        final Set<Proxy> resultSet = new HashSet<>();

        for (final String proxy : buffer)
        {
            final String ipAddress = proxy.substring(0, proxy.indexOf(DELIMITER));
            final int port = Integer.parseInt(proxy.substring(proxy.indexOf(DELIMITER) + 1));

            if (ipAddress.matches(IP_ADDRESS_REGULAR_EXPRESSION) && ((port > 80) && (port < 65_535)))
            {
                resultSet.add(new Proxy(ipAddress, port));
            }
        }

        return resultSet;
    }
}