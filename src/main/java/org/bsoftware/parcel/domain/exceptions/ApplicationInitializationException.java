package org.bsoftware.parcel.domain.exceptions;

/**
 * ApplicationInitializationException indicates that something went wrong application initialization
 *
 * @author Rudolf Barbu
 * @version 2
 */
public final class ApplicationInitializationException extends RuntimeException
{
    /**
     * Call super class with customized exception message
     *
     * @param pattern message pattern
     * @param argument argument for message pattern formatting
     */
    public ApplicationInitializationException(final String pattern, final String argument)
    {
        super(String.format(pattern, argument));
    }
}