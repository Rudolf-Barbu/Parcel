package org.bsoftware.parcel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * WorkType is an enum with all supported work-types
 *
 * @author Rudolf Barbu
 * @version 1.0.8
 */
@Getter
@RequiredArgsConstructor
public enum WorkType
{
    LOADING("Thread of %s loading"), BRUTEFORCE("Brute-force thread #%d");

    /**
     * Defines thread name pattern
     */
    private final String threadNamePattern;
}