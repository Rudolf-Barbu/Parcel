package org.bsoftware.parcel.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings(value = "MismatchedQueryAndUpdateOfCollection")
public final class DataContainerUtility
{
    private static final HashSet<Source> SOURCES = new HashSet<>();

    private static final HashSet<Proxy> PROXIES = new HashSet<>();

    public static synchronized void refreshSources(final Set<Source> sources)
    {
        SOURCES.clear();
        SOURCES.addAll(sources);
    }

    public static synchronized void refreshProxies(final Set<Proxy> proxies)
    {
        PROXIES.clear();
        PROXIES.addAll(proxies);
    }
}