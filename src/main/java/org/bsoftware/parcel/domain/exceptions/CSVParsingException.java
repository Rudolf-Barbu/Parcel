package org.bsoftware.parcel.domain.exceptions;

/**
 * CSVParsingException indicates that application can't parse the CSV file
 *
 * @author Rudolf Barbu
 * @version 2
 */
public final class CSVParsingException extends RuntimeException
{
    /**
     * Defines message pattern for exception
     */
    private static final String MESSAGE_PATTERN = "Can't parse CSV file, message: %s";

    /**
     * Call super class with customized exception message
     *
     * @param message parsing failure details
     */
    public CSVParsingException(final String message)
    {
        super(String.format(MESSAGE_PATTERN, message));
    }
}