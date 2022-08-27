package org.bsoftware.parcel.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bsoftware.parcel.domain.exceptions.CSVParsingException;
import org.bsoftware.parcel.domain.model.Connection;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ConnectionUtility class provides various connection operations
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@SuppressWarnings("DanglingJavadoc")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionUtility
{
    /**
     * Initializing list with possible connections
     */
    static
    {
        try
        {
            final Map<String, Connection> buffer = new HashMap<>();

            try (final InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(ConnectionUtility.class.getResourceAsStream("/connection.csv"))); final CSVParser csvParser = CSVFormat.Builder.create().setHeader(Header.class).build().parse(inputStreamReader))
            {
                for (final CSVRecord csvRecord : csvParser)
                {
                    final int port = Integer.parseInt(csvRecord.get(Header.PORT));
                    final boolean ssl = Boolean.parseBoolean(csvRecord.get(Header.SSL));
                    final boolean tls = Boolean.parseBoolean(csvRecord.get(Header.TLS));

                    buffer.put(csvRecord.get(Header.DOMAIN), new Connection(csvRecord.get(Header.HOST), port, ssl, tls));
                }
            }

            CONNECTION_MAP = buffer;
        }
        catch (final IOException ioException)
        {
            throw new CSVParsingException(ioException.getMessage());
        }
    }

    /**
     * Defines connection map
     */
    private static final Map<String, Connection> CONNECTION_MAP;

    /**
     * Defines email domain symbol
     */
    private static final char DOMAIN = '@';

    /**
     * Checks, if application can connect to this domain
     *
     * @param credential credentials to connect
     * @return true, if application is possible able to connect
     */
    public static synchronized boolean isConnectionSupported(final String credential)
    {
        return CONNECTION_MAP.containsKey(getDomain(credential));
    }

    /**
     * Gets connection object
     *
     * @param credential connection key
     * @return corresponding connection object
     */
    public static synchronized Connection getConnection(final String credential)
    {
        return CONNECTION_MAP.get(getDomain(credential));
    }

    /**
     * Returns domain from credential
     *
     * @param credentials incoming credentials
     * @return cut domain
     */
    private static String getDomain(final String credentials)
    {
        return credentials.substring(credentials.indexOf(DOMAIN));
    }

    /**
     * Enum, with all possible headers
     */
    private enum Header
    {
        DOMAIN, HOST, PORT, SSL, TLS
    }
}