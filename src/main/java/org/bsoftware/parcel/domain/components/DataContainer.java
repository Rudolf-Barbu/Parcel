package org.bsoftware.parcel.domain.components;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.ProxyRatingAdjustmentType;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.utilities.ValidationUtility;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DataContainer is a class, which holds application data and provides methods to manipulate it
 *
 * @author Rudolf Barbu
 * @version 1.0.15
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataContainer
{
    /**
     * Defines sources holder object
     */
    private static final Queue<Source> SOURCES = new ArrayDeque<>();

    /**
     * Defines proxies holder object
     */
    private static final Map<Proxy, Integer> PROXIES = new HashMap<>();

    /**
     * Defines random generator, to get proxies
     */
    private static final Random RANDOM_GENERATOR = new Random();

    /**
     * Refreshes data holder object, for corresponding data-type
     *
     * @param dataType particular data-type
     * @param data data to refresh
     */
    @SuppressWarnings("unchecked")
    public static void refreshData(final DataType dataType, final Collection<?> data)
    {
        if (dataType == DataType.SOURCE)
        {
            SOURCES.clear();
            SOURCES.addAll((Collection<Source>) data);
        }
        else
        {
            PROXIES.clear();
            PROXIES.putAll(((Collection<Proxy>) data).stream().collect(Collectors.toMap(Function.identity(), initialRating -> 0)));
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
     * Gets ByteBuffer buffer and fills it with all remaining source strings
     *
     * @return filled ByteBuffer
     */
    public static ByteBuffer getSourcesByteBuffer()
    {
        return ByteBuffer.wrap(SOURCES.stream().map(source -> String.format("%s:%s", source.getCredential(), source.getPassword()).concat(System.lineSeparator())).collect(Collectors.joining()).getBytes());
    }

    /**
     * Retrieves next element
     *
     * @return next proxy object
     */
    public static synchronized Proxy getConvectionProxy()
    {
        if (PROXIES.isEmpty())
        {
            throw new IllegalStateException("Proxies should be loaded firstly");
        }

        final int averageProxyRating = PROXIES.values().stream().mapToInt(Integer::intValue).sum() / PROXIES.size();
        final List<Proxy> applicableProxies = PROXIES.entrySet().stream().filter(entry -> (entry.getValue() >= averageProxyRating)).map(Map.Entry::getKey).collect(Collectors.toList());

        return applicableProxies.get(RANDOM_GENERATOR.nextInt(applicableProxies.size()));
    }

    /**
     * Changes the rating of a specific proxy, by one step
     *
     * @param proxy target proxy
     * @param proxyRatingAdjustmentType rating adjustment value
     */
    public static void adjustProxyRating(final Proxy proxy, final ProxyRatingAdjustmentType proxyRatingAdjustmentType)
    {
        PROXIES.put(proxy, PROXIES.get(proxy) + proxyRatingAdjustmentType.getProxyRatingAdjustmentValue());
    }

    /**
     * Checks if particular data is empty
     *
     * @param dataTypes particular data-types
     * @return true, if all listed data-types are empty
     */
    @SuppressWarnings("SimplifyStreamApiCallChains")
    public static boolean isDataEmpty(final DataType... dataTypes)
    {
        ValidationUtility.validateEnumArguments(dataTypes);
        return Arrays.stream(dataTypes).map(dataType -> (dataType == DataType.SOURCE) ? SOURCES.isEmpty() : PROXIES.isEmpty()).allMatch(Boolean.TRUE::equals);
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