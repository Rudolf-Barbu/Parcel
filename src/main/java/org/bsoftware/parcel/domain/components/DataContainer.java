package org.bsoftware.parcel.domain.components;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * DataContainer is a class, which holds application data and provides methods to manipulate it
 *
 * @author Rudolf Barbu
 * @version 1.0.7
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataContainer
{
    /**
     * Defines sources holder object
     */
    private static final Queue<Source> SOURCES = new PriorityQueue<>();

    /**
     * Defines proxies holder object
     */
    private static final Queue<Proxy> PROXIES = new PriorityQueue<>();

    /**
     * Refreshes data holder object, for corresponding data-type
     *
     * @param dataType - particular data-type
     * @param data - data to refresh
     */
    @SuppressWarnings("unchecked")
    public static void refreshData(final DataType dataType, final Set<?> data)
    {
        if (dataType == DataType.SOURCE)
        {
            SOURCES.clear();
            SOURCES.addAll((Set<Source>) data);
        }
        else if (dataType == DataType.PROXY)
        {
            PROXIES.clear();
            PROXIES.addAll((Set<Proxy>) data);
        }
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
     * Retrieves next element
     *
     * @return next proxy object
     */
    public static synchronized Proxy getNextProxy()
    {
        return PROXIES.poll();
    }

    /**
     * Checks if particular data is empty
     *
     * @param dataTypes - particular data-types
     * @return trye, if all listed data-types are empty
     */
    public static boolean isDataEmpty(final DataType... dataTypes)
    {
        if ((dataTypes.length == 0) || (dataTypes.length > 2))
        {
            throw new IllegalArgumentException("WorkTypes length is out on ranges");
        }
        else if (Arrays.stream(dataTypes).distinct().count() < dataTypes.length)
        {
            throw new IllegalArgumentException("You can't pass the same dataType several times");
        }

        final List<Boolean> booleanList = new ArrayList<>();
        Arrays.stream(dataTypes).forEach(dataType -> booleanList.add((dataType == DataType.SOURCE) ? SOURCES.isEmpty() : PROXIES.isEmpty()));

        return booleanList.stream().allMatch(Boolean.TRUE::equals);
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