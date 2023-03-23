package org.bsoftware.parcel.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Source is a class that is used for representing source item
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Source implements Comparable<Source>
{
    /**
     * Container for credential data
     */
    private final String credential;

    /**
     * Container for password field
     */
    private final String password;

    /**
     * Comparing one source to another, using credential
     *
     * @param sourceToCompareWith source to compare
     * @return ratio, between two compared objects
     */
    @Override
    public int compareTo(final Source sourceToCompareWith)
    {
        return this.credential.compareTo(sourceToCompareWith.credential);
    }
}