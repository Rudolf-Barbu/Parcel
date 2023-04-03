package org.bsoftware.parcel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Source is a class that is used for representing source item
 *
 * @author Rudolf Barbu
 * @version 4
 */
@Getter
@RequiredArgsConstructor
public final class Proxy
{
    /**
     * Container for IP-address
     */
    private final String ipAddress;

    /**
     * Container for Port field
     */
    private final int port;
}