package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;

import java.util.Set;

/**
 * DataProcessingCallback is an interface that is used for notifying service about actions in DataProcessingRunnable
 *
 * @author Rudolf Barbu
 * @version 1.0.5
 */
public interface DataProcessingCallback
{
    /**
     * Called, then message transmitted
     *
     * @param message - message, which is transmitted to service
     */
    void handleDataProcessingMessage(final LogView.LogLevel logLevel, final String message);

    /**
     * Called, then data is successfully processed
     *
     * @param processedData - set with processed data
     * @param dataType - data type, which presented in processed data set
     * @param elapsedTimeInMilliseconds - execution time
     */
    void handleProcessedData(final Set<?> processedData, final DataType dataType, final long elapsedTimeInMilliseconds);
}