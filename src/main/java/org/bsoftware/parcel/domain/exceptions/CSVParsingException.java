package org.bsoftware.parcel.domain.exceptions;

/**
 * CSVParsingException indicates that application can't parse the CSV file
 *
 * @author Rudolf Barbu
 * @version 1.0.1
 */
public class CSVParsingException extends RuntimeException
{
    /**
     * Defines message pattern for exception
     */
    private static final String MESSAGE_PATTERN = "Can't parse CSV file, clause: %s";

    /**
     * Call super class with customized exception message
     *
     * @param details - parsing failure details
     */
    public CSVParsingException(final String details)
    {
        super(String.format(MESSAGE_PATTERN, details));
    }
}