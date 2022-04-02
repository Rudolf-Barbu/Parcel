package org.bsoftware.parcel.mvc.services;

import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataProcessingCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.domain.runnables.DataProcessingRunnable;

import java.io.File;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;

/**
 * MainService class is used for UI manipulation and thread creation
 *
 * @author Rudolf Barbu
 * @version 1.0.1
 */
@RequiredArgsConstructor
public class MainService implements DataProcessingCallback
{
    /**
     * Container for data processing threads
     */
    private static final EnumMap<DataType, Thread> DATA_PROCESSING_THREAD_MAP = new EnumMap<>(DataType.class);

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
                final Thread newThread = new Thread(dataProcessingRunnable);

                newThread.setDaemon(true);
                newThread.setPriority(Thread.NORM_PRIORITY);
                newThread.setName(String.format("Thread of %s processing ", dataType.getDataTypeNameInPlural()));

                DATA_PROCESSING_THREAD_MAP.put(dataType, newThread);
                newThread.start();
            }
            else
            {
                logViewLog.warning("Cannot run two same tasks in parallel");
            }
        }
        else
        {
            logViewLog.warning("Operation cancelled by user");
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
    public void handleProcessedData(final HashSet<?> processedData, final DataType dataType, final long elapsedTimeInMilliseconds)
    {
        if (processedData.isEmpty())
        {
            Platform.runLater(() -> logViewLog.warning(String.format("File with %s returned empty set", dataType.getDataTypeNameInPlural())));
            return;
        }

        if (dataType == DataType.SOURCE)
        {
            DataContainer.refreshSources((HashSet<Source>) processedData);
            Platform.runLater(() -> labelSources.setText(String.valueOf(processedData.size())));
        }
        else if (dataType == DataType.PROXY)
        {
            DataContainer.refreshProxies((HashSet<Proxy>) processedData);
            Platform.runLater(() -> labelProxies.setText(String.valueOf(processedData.size())));
        }

        Platform.runLater(() -> logViewLog.fine(String.format("File with %s processed in %d ms", dataType.getDataTypeNameInPlural(), elapsedTimeInMilliseconds)));
    }

    /**
     * Prints exception message to LogView
     *
     * @param exceptionMessage - message, which is transmitted to service
     */
    @Override
    public void handleProcessingExceptionMessage(final String exceptionMessage)
    {
        Platform.runLater(() -> logViewLog.error(exceptionMessage));
    }
}