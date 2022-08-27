package org.bsoftware.parcel.domain.exceptions;

/**
 * BinaryFileException indicates that something went wrong with binary file
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public final class BinaryFileException extends RuntimeException
{
    /**
     * Call super class with customized exception message
     *
     * @param pattern message pattern
     * @param argument argument for message pattern formatting
     */
    public BinaryFileException(final String pattern, final String argument)
    {
        super(String.format(pattern, argument));
    }
}