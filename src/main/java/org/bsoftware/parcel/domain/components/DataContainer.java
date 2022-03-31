package org.bsoftware.parcel.domain.components;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.util.HashSet;
import java.util.Set;

/**
 * DataContainer is a class, which holds application data and provides methods to manipulate it
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings(value = "MismatchedQueryAndUpdateOfCollection")
public final class DataContainer
{
    /**
     * Sources holder object
     */
    private static final HashSet<Source> SOURCES = new HashSet<>();

    /**
     * Proxies holder object
     */
    private static final HashSet<Proxy> PROXIES = new HashSet<>();

    /**
     * Refreshes sources holder object
     *
     * @param sources - new data payload
     */
    public static synchronized void refreshSources(final Set<Source> sources)
    {
        SOURCES.clear();
        SOURCES.addAll(sources);
    }

    /**
     * Refreshes proxies holder object
     *
     * @param proxies - new data payload
     */
    public static synchronized void refreshProxies(final Set<Proxy> proxies)
    {
        PROXIES.clear();
        PROXIES.addAll(proxies);
    }
}