package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.model.DataType;

import java.util.HashSet;

/**
 * DataProcessingCallback is an interface that is used for notifying service about actions in DataProcessingRunnable
 *
 * @author Rudolf Barbu
 * @version 1.0.1
 */
public interface DataProcessingCallback
{
    /**
     * Called, then data is successfully processed
     *
     * @param processedData - set with processed data
     * @param dataType - data type, which presented in processed data set
     * @param elapsedTimeInMilliseconds - execution time
     */
    void handleProcessedData(final HashSet<?> processedData, final DataType dataType, final long elapsedTimeInMilliseconds);
}