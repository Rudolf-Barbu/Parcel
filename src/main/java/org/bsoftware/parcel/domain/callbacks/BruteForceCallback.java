package org.bsoftware.parcel.domain.callbacks;

import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;

/**
 * BruteForceCallback is an interface that is used for notifying service about actions in BruteForceRunnable
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
public interface BruteForceCallback
{
    /**
     * Used to decrement counters
     *
     * @param dataType data-type, to determine right counter object
     */
    void handleDecrementCounter(final DataType dataType);

    /**
     * Called, then message transmitted
     *
     * @param message message, which is transmitted to service
     */
    void handleBruteForceMessage(final LogLevel logLevel, final String message);

    /**
     * Called, on last stage of bruteforce algorithm
     */
    void handleThreadInterruption();
}