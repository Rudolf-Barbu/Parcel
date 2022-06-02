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
public class Proxy implements Comparable<Proxy>
{
    /**
     * Container for IP-address
     */
    private final String ipAddress;

    /**
     * Container for Port field
     */
    private final int port;

    /**
     * Comparing one proxy to another, using IP-address
     *
     * @param proxyToCompareWith - proxy to compare
     * @return ratio, between two compared objects
     */
    @Override
    public int compareTo(final Proxy proxyToCompareWith)
    {
        return this.ipAddress.compareTo(proxyToCompareWith.ipAddress);
    }
}