package org.bsoftware.parcel.domain.runnables;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.bsoftware.parcel.domain.callbacks.DataProcessingCallback;
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
 * @version 1.0.5
 */
@RequiredArgsConstructor
public class DataProcessingRunnable implements Runnable
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
    private static final String IP_ADDRESS_REGULAR_EXPRESSION = "([\\d]{1,3}\\.){3}[\\d]{1,3}";

    /**
     * Target file, for processing
     */
    private final File file;

    /**
     * Data type for validation
     */
    private final DataType dataType;

    /**
     * Callback interface, which is used to deliver messages to service
     */
    private final DataProcessingCallback dataProcessingCallback;

    /**
     * Main method of processing algorithm
     */
    @Override
    public void run()
    {
        final long pointcut = System.nanoTime();

        try
        {
            final String detectedFileType = new Tika().detect(file);

            if (!detectedFileType.equals("text/plain"))
            {
                dataProcessingCallback.handleDataProcessingMessage(LogView.LogLevel.ERROR, String.format("Only text files allowed, given: %s", detectedFileType));
                return;
            }

            final List<String> dataBuffer = Files.readAllLines(file.toPath()).stream().filter(entry -> (!entry.isEmpty() && ((entry.indexOf(DELIMITER) != -1) && (entry.indexOf(DELIMITER) == entry.lastIndexOf(DELIMITER))))).collect(Collectors.toList());
            final int totalBufferLines = dataBuffer.size();

            if (totalBufferLines > MAX_LINES_ALLOWED)
            {
                dataProcessingCallback.handleDataProcessingMessage(LogView.LogLevel.ERROR, String.format("Maximum number of lines allowed: %d, given: %d", MAX_LINES_ALLOWED, totalBufferLines));
                return;
            }

            dataProcessingCallback.handleProcessedData((dataType == DataType.SOURCE) ? processSources(dataBuffer) : processProxies(dataBuffer), dataType, (System.nanoTime() - pointcut) / 1_000_000);
        }
        catch (final IOException ioException)
        {
            dataProcessingCallback.handleDataProcessingMessage(LogView.LogLevel.ERROR, String.format("IO exception occurred, clause: %s", ioException.getMessage()));
        }
    }

    /**
     * Source validation method
     *
     * @param dataBuffer - list, which contains unprocessed lines
     * @return unique set of processed sources
     */
    private Set<Source> processSources(final List<String> dataBuffer)
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
     * @return unique set of processed proxies
     */
    private Set<Proxy> processProxies(final List<String> dataBuffer)
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