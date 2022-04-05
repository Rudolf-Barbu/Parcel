package org.bsoftware.parcel.mvc.services;

import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataProcessingCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.domain.runnables.BruteForceRunnable;
import org.bsoftware.parcel.domain.runnables.DataProcessingRunnable;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MainService class is used for UI manipulation and thread creation
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@RequiredArgsConstructor
public class MainService implements DataProcessingCallback
{
    /**
     * Container for data processing threads
     */
    private static final Map<DataType, Thread> DATA_PROCESSING_THREAD_MAP = new EnumMap<>(DataType.class);

    /**
     * Container for brute-force threads
     */
    @SuppressWarnings(value = "MismatchedReadAndWriteOfArray")
    private static final Thread[] BRUTE_FORCE_THREAD_ARRAY = new Thread[20];

    /**
     * Sources counter
     */
    private final Label labelSources;

    /**
     * Proxies counter
     */
    private final Label labelProxies;

    /**
     * Custom log container
     */
    private final LogView logViewLog;

    /**
     * Processes data, using asynchronous mechanisms
     *
     * @param optionalFile - file to process
     * @param dataType - data type, on which validation depends
     */
    @SuppressWarnings(value = "OptionalUsedAsFieldOrParameterType")
    public void processData(final Optional<File> optionalFile, final DataType dataType)
    {
        if (optionalFile.isPresent())
        {
            final Thread oldThread = DATA_PROCESSING_THREAD_MAP.get(dataType);

            if ((oldThread == null) || (oldThread.getState() == Thread.State.TERMINATED))
            {
                final DataProcessingRunnable dataProcessingRunnable = new DataProcessingRunnable(optionalFile.get(), dataType, this);
                final Thread newThread = createDaemonThread(dataProcessingRunnable, String.format("Thread of %s processing", dataType.getDataTypeNameInPlural()));

                DATA_PROCESSING_THREAD_MAP.put(dataType, newThread);
                newThread.start();
            }
            else
            {
                logViewLog.log(LogLevel.WARNING, "Cannot run two same tasks in parallel");
            }
        }
        else
        {
            logViewLog.log(LogLevel.WARNING, "Operation cancelled by user");
        }
    }

    /**
     * Creating new threads, using BruteForceRunnable
     */
    public synchronized void start()
    {
        if (!DataContainer.isAllDataLoaded())
        {
            logViewLog.log(LogLevel.WARNING, "Load the proxies and sources, before starting");
            return;
        }

        logViewLog.log(LogLevel.INFO, "Work started");

        for (int index = 0; index < BRUTE_FORCE_THREAD_ARRAY.length; index++)
        {
            final BruteForceRunnable bruteForceRunnable = new BruteForceRunnable();
            final Thread thread = createDaemonThread(bruteForceRunnable, String.format("Brute-force thread #%d", index));

            BRUTE_FORCE_THREAD_ARRAY[index] = thread;
            thread.start();
        }
    }

    /**
     * Saving processed data and updating counters
     *
     * @param processedData - set with processed data
     * @param dataType - data type, which presented in processed data set
     * @param elapsedTimeInMilliseconds - execution time
     */
    @Override
    @SuppressWarnings(value = "unchecked")
    public void handleProcessedData(final Set<?> processedData, final DataType dataType, final long elapsedTimeInMilliseconds)
    {
        if (processedData.isEmpty())
        {
            Platform.runLater(() -> logViewLog.log(LogLevel.WARNING, String.format("File with %s returned empty set", dataType.getDataTypeNameInPlural())));
            return;
        }

        if (dataType == DataType.SOURCE)
        {
            DataContainer.refreshSources((Set<Source>) processedData);
            Platform.runLater(() -> labelSources.setText(String.valueOf(processedData.size())));
        }
        else if (dataType == DataType.PROXY)
        {
            DataContainer.refreshProxies((Set<Proxy>) processedData);
            Platform.runLater(() -> labelProxies.setText(String.valueOf(processedData.size())));
        }

        Platform.runLater(() -> logViewLog.log(LogLevel.FINE, String.format("File with %s processed in %d ms", dataType.getDataTypeNameInPlural(), elapsedTimeInMilliseconds)));
    }

    /**
     * Prints message to LogView
     *
     * @param message - message, which is transmitted to service
     */
    @Override
    public void handleDataProcessingMessage(final LogLevel logLevel, final String message)
    {
        Platform.runLater(() -> logViewLog.log(logLevel, message));
    }

    /**
     * Creates and returns new daemon thread, with custom name
     *
     * @param runnable - separated runnable logic
     * @param threadName - string, which will represent thread name
     * @return newly created, daemon thread
     */
    private Thread createDaemonThread(final Runnable runnable, final String threadName)
    {
        final Thread thread = new Thread(runnable);

        thread.setDaemon(Boolean.TRUE);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setName(threadName);

        return thread;
    }
}