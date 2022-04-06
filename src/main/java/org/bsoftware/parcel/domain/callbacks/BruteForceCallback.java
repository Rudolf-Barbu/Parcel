package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;

/**
 * BruteForceCallback is an interface that is used for notifying service about actions in BruteForceRunnable
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public interface BruteForceCallback
{
    /**
     * Used to decrement counters
     *
     * @param dataType - data type, to determine right counter object
     */
    void decrementCounter(final DataType dataType);

    /**
     * Called, then message transmitted
     *
     * @param message - message, which is transmitted to service
     */
    void handleBruteForceMessage(final LogView.LogLevel logLevel, final String message);
}