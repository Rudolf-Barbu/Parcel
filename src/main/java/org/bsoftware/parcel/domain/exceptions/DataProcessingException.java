package org.bsoftware.parcel.domain.exceptions;

/**
 * OperatingSystemNotSupportedException indicates that data cannot be properly processed
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public final class DataProcessingException extends RuntimeException
{
    /**
     * Call super class with customized exception message
     *
     * @param message - customized message
     */
    public DataProcessingException(final String message)
    {
        super(message);
    }
}