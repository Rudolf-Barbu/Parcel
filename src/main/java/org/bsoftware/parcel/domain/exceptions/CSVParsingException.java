package org.bsoftware.parcel.domain.exceptions;

/**
 * CSVParsingException indicates that application can't parse the CSV file
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public class CSVParsingException extends RuntimeException
{
    /**
     * Defines message pattern for exception
     */
    public static final String PATTERN = "Can't parse CSV file, clause: %s";

    /**
     * Call super class with customized exception message
     *
     * @param details - parsing failure details
     */
    public CSVParsingException(final String details)
    {
        super(String.format(PATTERN, details));
    }
}