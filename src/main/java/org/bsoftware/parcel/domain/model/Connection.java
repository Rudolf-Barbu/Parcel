package org.bsoftware.parcel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Connection is a class that is used for representing connection settings
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public final class Connection
{
    /**
     * Container for host
     */
    private final String host;

    /**
     * Container for port
     */
    private final int port;

    /**
     * Container for SSL switch
     */
    private final boolean ssl;

    /**
     * Container for TLS switch
     */
    private final boolean tls;
}