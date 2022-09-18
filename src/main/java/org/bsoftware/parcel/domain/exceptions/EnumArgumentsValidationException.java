package org.bsoftware.parcel.domain.exceptions;

/**
 * EnumArgumentsValidationException indicates that enum method arguments are not valid
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public final class EnumArgumentsValidationException extends Exception
{
    /**
     * Defines message pattern for exception
     */
    private static final String MESSAGE_PATTERN = "Can't validate enum arguments, message: %s";

    /**
     * Call super class with customized exception message
     *
     * @param message parsing failure details
     */
    public EnumArgumentsValidationException(final String message)
    {
        super(String.format(MESSAGE_PATTERN, message));
    }
}