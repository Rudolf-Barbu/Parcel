package org.bsoftware.parcel.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Source is a class that is used for representing source item
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Source
{
    /**
     * Container for credential data
     */
    private final String credential;

    /**
     * Password field
     */
    private final String password;
}