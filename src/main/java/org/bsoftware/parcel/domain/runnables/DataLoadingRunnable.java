package org.bsoftware.parcel.domain.runnables;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

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
 * @version 1.0.6
 */
@RequiredArgsConstructor
public class DataLoadingRunnable implements Runnable
{
    /**
     * Defines source and proxy line delimiter
     */
    private static final char DELIMITER = ':';

    /**
     * Defines max lines allowed to contain in a processing file
     */
    private static final int MAX_LINES_ALLOWED = 16_777_216;

    /**
     * Defines regular expression for source's credential
     */
    private static final String CREDENTIAL_REGULAR_EXPRESSION = "[\\w.-]{5,30}@(t-online|magenta)\\.de";

    /**
     * Defines regular expression for source's password
     */
    private static final String PASSWORD_REGULAR_EXPRESSION = "[^;]{8,16}";

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
            if (!new Tika().detect(file).equals("text/plain"))
            {
                dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, "Only text files allowed");
                return;
            }

            final List<String> dataBuffer = Files.readAllLines(file.toPath()).stream().filter(this::lineFilter).collect(Collectors.toList());

            if (dataBuffer.size() > MAX_LINES_ALLOWED)
            {
                dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, "Exceeded line count limit");
                return;
            }

            dataLoadingCallback.handleLoadedData((dataType == DataType.SOURCE) ? loadSources(dataBuffer) : loadProxies(dataBuffer), dataType);
        }
        catch (final IOException ioException)
        {
            dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.ERROR, String.format("IO exception occurred, clause: %s", ioException.getMessage()));
        }
    }

    /**
     * Filter predicate for data buffer
     *
     * @param line - particular line, to be filtered
     * @return frue, if line passes the filter
     */
    private boolean lineFilter(final String line)
    {
        return (!line.isEmpty() && ((line.indexOf(DELIMITER) != -1) && (line.indexOf(DELIMITER) == line.lastIndexOf(DELIMITER))));
    }

    /**
     * Source validation method
     *
     * @param dataBuffer - list, which contains unprocessed lines
     * @return unique set of loaded sources
     */
    private Set<Source> loadSources(final List<String> dataBuffer)
    {
        final Set<Source> resultSet = new HashSet<>();

        dataBuffer.forEach(source ->
        {
            final String credential = source.substring(0, source.indexOf(DELIMITER));
            if (!credential.matches(CREDENTIAL_REGULAR_EXPRESSION))
            {
                return;
            }

            final String password = source.substring(source.indexOf(DELIMITER) + 1);
            if (!password.matches(PASSWORD_REGULAR_EXPRESSION))
            {
                return;
            }

            resultSet.add(new Source(credential, password));
        });

        return resultSet;
    }

    /**
     * Proxy validation method
     *
     * @param dataBuffer - list, which contains unprocessed lines
     * @return unique set of loaded proxies
     */
    private Set<Proxy> loadProxies(final List<String> dataBuffer)
    {
        final Set<Proxy> resultSet = new HashSet<>();

        dataBuffer.forEach(proxy ->
        {
            final String ipAddress = proxy.substring(0, proxy.indexOf(DELIMITER));
            if (!ipAddress.matches(IP_ADDRESS_REGULAR_EXPRESSION))
            {
                return;
            }

            final int port = Integer.parseInt(proxy.substring(proxy.indexOf(DELIMITER) + 1));
            if ((port < 80) || (port > 65_535))
            {
                return;
            }

            resultSet.add(new Proxy(ipAddress, port));
        });

        return resultSet;
    }
}