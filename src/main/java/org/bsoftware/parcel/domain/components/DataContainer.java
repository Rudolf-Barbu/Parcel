package org.bsoftware.parcel.domain.components;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * DataContainer is a class, which holds application data and provides methods to manipulate it
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataContainer
{
    /**
     * Sources holder object
     */
    private static final Queue<Source> SOURCES = new PriorityQueue<>();

    /**
     * Proxies holder object
     */
    private static final Queue<Proxy> PROXIES = new PriorityQueue<>();

    /**
     * Refreshes sources holder object
     *
     * @param sources - new data payload
     */
    public static void refreshSources(final Set<Source> sources)
    {
        SOURCES.clear();
        SOURCES.addAll(sources);
    }

    /**
     * Retrieves next element
     *
     * @return next source object
     */
    public static synchronized Source getNextSource()
    {
        return SOURCES.poll();
    }

    /**
     * Refreshes proxies holder object
     *
     * @param proxies - new data payload
     */
    public static void refreshProxies(final Set<Proxy> proxies)
    {
        PROXIES.clear();
        PROXIES.addAll(proxies);
    }

    /**
     * Retrieves next element
     *
     * @return next proxy object
     */
    public static synchronized Proxy getNextProxy()
    {
        return PROXIES.poll();
    }

    /**
     * Checks if all data has been loaded
     *
     * @return boolean, depends on loaded all data or not
     */
    public static boolean isDataEmpty()
    {
        return (SOURCES.isEmpty() && PROXIES.isEmpty());
    }

    /**
     * Clears all data
     */
    public static void clearData()
    {
        SOURCES.clear();
        PROXIES.clear();
    }
}