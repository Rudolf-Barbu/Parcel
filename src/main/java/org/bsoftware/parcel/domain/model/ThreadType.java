package org.bsoftware.parcel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * WorkType is an enum with all supported work-types
 *
 * @author Rudolf Barbu
 * @version 9
 */
@RequiredArgsConstructor
public enum ThreadType
{
    LOADING("Thread of %s loading"), BRUTEFORCE("Brute-force thread #%d");

    /**
     * Defines thread name pattern
     */
    @Getter
    private final String threadNamePattern;
}