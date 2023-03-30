package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;

import java.util.Collection;

/**
 * DataProcessingCallback is an interface that is used for notifying service about actions in DataLoadingRunnable
 *
 * @author Rudolf Barbu
 * @version 8
 */
public interface DataLoadingCallback
{
    /**
     * Called, then message transmitted
     *
     * @param message message, which is transmitted to service
     */
    void handleDataLoadingMessage(final LogLevel logLevel, final String message);

    /**
     * Called, then data is successfully loaded
     *
     * @param loadedData collection with loaded data
     * @param dataType data-type, which presented in loaded data set
     */
    void handleLoadedData(final Collection<?> loadedData, final DataType dataType);
}