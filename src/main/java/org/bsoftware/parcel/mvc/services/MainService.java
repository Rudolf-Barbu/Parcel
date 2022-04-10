package org.bsoftware.parcel.mvc.services;

import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.callbacks.DataProcessingCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.domain.runnables.BruteForceRunnable;
import org.bsoftware.parcel.domain.runnables.DataProcessingRunnable;

import java.io.File;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MainService class is used for UI manipulation and thread creation
 *
 * @author Rudolf Barbu
 * @version 1.0.4
 */
@RequiredArgsConstructor
public class MainService implements DataProcessingCallback, BruteForceCallback
{
    /**
     * Container for data processing threads
     */
    private static final Map<DataType, Thread> DATA_PROCESSING_THREAD_MAP = new EnumMap<>(DataType.class);

    /**
     * Container for brute-force threads
     */
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
            if (isThreadNotTerminated(DATA_PROCESSING_THREAD_MAP.get(dataType)) || isWorkNotTerminated())
            {
                logViewLog.log(LogView.LogLevel.WARNING, "Cannot run two same processing tasks or/and brute-force in parallel");
                return;
            }

            final DataProcessingRunnable dataProcessingRunnable = new DataProcessingRunnable(optionalFile.get(), dataType, this);
            final Thread newThread = createDaemonThread(dataProcessingRunnable, String.format("Thread of %s processing", dataType.getDataTypeNameInPlural()));

            DATA_PROCESSING_THREAD_MAP.put(dataType, newThread);
            newThread.start();
        }
        else
        {
            logViewLog.log(LogView.LogLevel.WARNING, "Operation cancelled by user");
        }
    }

    /**
     * Creating new threads, using BruteForceRunnable
     */
    public synchronized void start()
    {
        if (!DataContainer.isAllDataLoaded() || isWorkNotTerminated())
        {
            logViewLog.log(LogView.LogLevel.WARNING, "Load data or/and wait for work being terminated");
            return;
        }

        final LocalTime startTime = LocalTime.now();

        for (int index = 0; index < BRUTE_FORCE_THREAD_ARRAY.length; index++)
        {
            final BruteForceRunnable bruteForceRunnable = new BruteForceRunnable(startTime, this);
            final Thread thread = createDaemonThread(bruteForceRunnable, String.format("Brute-force thread #%d", index));

            BRUTE_FORCE_THREAD_ARRAY[index] = thread;
            thread.start();
        }

        logViewLog.log(LogView.LogLevel.INFO, "Work started");
    }

    /**
     * Interrupts brute-force threads
     */
    public void terminate()
    {
        if (isWorkNotTerminated())
        {
            Arrays.stream(BRUTE_FORCE_THREAD_ARRAY).forEach(Thread::interrupt);
            return;
        }

        logViewLog.log(LogView.LogLevel.WARNING, "Work already terminated, or not started yet");
    }

    /**
     * Clears sources and proxies
     */
    public void clear()
    {
        if (isWorkNotTerminated())
        {
            logViewLog.log(LogView.LogLevel.WARNING, "Cannot clear data, while work is not terminated");
            return;
        }

        clearDataAndCounters();
        logViewLog.log(LogView.LogLevel.INFO, "Data cleared");
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
            Platform.runLater(() -> logViewLog.log(LogView.LogLevel.WARNING, String.format("File with %s returned empty set", dataType.getDataTypeNameInPlural())));
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

        Platform.runLater(() -> logViewLog.log(LogView.LogLevel.FINE, String.format("File with %s processed in %d ms", dataType.getDataTypeNameInPlural(), elapsedTimeInMilliseconds)));
    }

    /**
     * Prints message to LogView
     *
     * @param message - message, which is transmitted to service
     */
    @Override
    public void handleDataProcessingMessage(final LogView.LogLevel logLevel, final String message)
    {
        Platform.runLater(() -> logViewLog.log(logLevel, message));
    }

    /**
     * Decrements one of counters, depending on data type
     *
     * @param dataType - data type, to determine right counter object
     */
    @Override
    public void handleDecrementCounter(final DataType dataType)
    {
        final Label targetObject = (dataType == DataType.SOURCE) ? labelSources : labelProxies;
        Platform.runLater(() -> targetObject.setText(String.valueOf(Integer.parseInt(targetObject.getText()) - 1)));
    }

    /**
     * Prints message to LogView
     *
     * @param message - message, which is transmitted to service
     */
    @Override
    public void handleBruteForceMessage(final LogView.LogLevel logLevel, final String message)
    {
        Platform.runLater(() -> logViewLog.log(logLevel, message));
    }

    /**
     * Handles thread termination, and prints log
     */
    @Override
    public synchronized void handleThreadTermination()
    {
        final long terminatedThreads = Arrays.stream(BRUTE_FORCE_THREAD_ARRAY).filter(thread -> thread.getState() == Thread.State.TERMINATED).count();

        if ((BRUTE_FORCE_THREAD_ARRAY.length - terminatedThreads) == 1)
        {
            clearDataAndCounters();
            Platform.runLater(() -> logViewLog.log(LogView.LogLevel.INFO, "Work terminated"));
        }
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

    /**
     * Checks, if thread still executing
     *
     * @param thread - target thread
     * @return true, if thread is not terminated
     */
    private boolean isThreadNotTerminated(final Thread thread)
    {
        return ((thread != null) && (thread.getState() != Thread.State.TERMINATED));
    }

    /**
     * Checks if brute-force threads are still executing
     *
     * @return true, if work terminated
     */
    private boolean isWorkNotTerminated()
    {
        for (final Thread thread : BRUTE_FORCE_THREAD_ARRAY)
        {
            if (isThreadNotTerminated(thread))
            {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Clears data and resets counters
     */
    private void clearDataAndCounters()
    {
        DataContainer.clearData();
        Platform.runLater(() -> labelSources.setText("0"));
        Platform.runLater(() -> labelProxies.setText("0"));
    }
}