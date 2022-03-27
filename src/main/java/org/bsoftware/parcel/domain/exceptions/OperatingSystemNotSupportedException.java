package org.bsoftware.parcel.domain.exceptions;

/**
 * OperatingSystemNotSupportedException indicates that current operating system is unsupported
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public final class OperatingSystemNotSupportedException extends RuntimeException
{
    /**
     * Call super class with customized exception message
     *
     * @param operatingSystem - name of unsupported operating system
     */
    public OperatingSystemNotSupportedException(final String operatingSystem)
    {
        super(String.format("Current operating system: %s is not supported", operatingSystem));
    }
}