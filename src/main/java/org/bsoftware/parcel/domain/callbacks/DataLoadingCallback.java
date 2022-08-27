package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;

import java.util.Set;

/**
 * DataProcessingCallback is an interface that is used for notifying service about actions in DataLoadingRunnable
 *
 * @author Rudolf Barbu
 * @version 1.0.6
 */
public interface DataLoadingCallback
{
    /**
     * Called, then message transmitted
     *
     * @param message message, which is transmitted to service
     */
    void handleDataLoadingMessage(final LogView.LogLevel logLevel, final String message);

    /**
     * Called, then data is successfully loaded
     *
     * @param loadedData set with loaded data
     * @param dataType data-type, which presented in loaded data set
     */
    void handleLoadedData(final Set<?> loadedData, final DataType dataType);
}