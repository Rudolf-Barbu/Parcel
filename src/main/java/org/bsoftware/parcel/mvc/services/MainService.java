package org.bsoftware.parcel.mvc.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.tika.Tika;
import org.bsoftware.parcel.domain.exceptions.DataProcessingException;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MainService
{
    @Getter
    private static final MainService INSTANCE = new MainService();

    private static final String CREDENTIAL_REGULAR_EXPRESSION = "[\\w.-]{5,31}@(mail|inbox|list|bk|internet)\\.ru";

    private static final String PASSWORD_REGULAR_EXPRESSION = "[^;]{8,40}";

    private static final String IP_ADDRESS_REGULAR_EXPRESSION = "([\\d]{1,3}\\.){3}[\\d]{1,3}";

    private static final int MAX_LINES_ALLOWED = 524_288;

    private static final char DELIMITER = ':';

    private final HashSet<Source> sources = new HashSet<>();

    private final HashSet<Proxy> proxies = new HashSet<>();

    public int processData(final File file, final DataType dataType) throws DataProcessingException
    {
        try
        {
            final String detectedFileType = new Tika().detect(file);

            if (!detectedFileType.equals("text/plain"))
            {
                throw new DataProcessingException(String.format("Only text files allowed, given: %s", detectedFileType));
            }

            final List<String> dataBuffer = Files.readAllLines(file.toPath()).stream().filter(entry -> (!entry.isEmpty() && ((entry.indexOf(DELIMITER) != -1) && (entry.indexOf(DELIMITER) == entry.lastIndexOf(DELIMITER))))).collect(Collectors.toList());
            final int totalBufferLines = dataBuffer.size();

            if ((dataBuffer.isEmpty()) || (totalBufferLines > MAX_LINES_ALLOWED))
            {
                throw new DataProcessingException(String.format("Number of lines is out of bounds (1 - %d), given: %d", MAX_LINES_ALLOWED, totalBufferLines));
            }

            return (dataType == DataType.SOURCE) ? processSources(dataBuffer) : processProxies(dataBuffer);
        }
        catch (IOException ioException)
        {
            throw new DataProcessingException(String.format("IO exception occurred, clause: %s", ioException.getMessage()));
        }
    }

    private int processSources(final List<String> dataBuffer)
    {
        sources.clear();

        dataBuffer.forEach(source ->
        {
            final String credential = source.substring(0, source.indexOf(DELIMITER));
            if (!credential.matches(CREDENTIAL_REGULAR_EXPRESSION))
            {
                return;
            }

            final String password = source.substring(source.indexOf(DELIMITER) + 1);
            if (!password.matches(PASSWORD_REGULAR_EXPRESSION))
            {
                return;
            }

            sources.add(new Source(credential, password));
        });

        return sources.size();
    }

    private int processProxies(final List<String> dataBuffer)
    {
        proxies.clear();

        dataBuffer.forEach(proxy ->
        {
            final String ipAddress = proxy.substring(0, proxy.indexOf(DELIMITER));
            if (!ipAddress.matches(IP_ADDRESS_REGULAR_EXPRESSION))
            {
                return;
            }

            final int port = Integer.parseInt(proxy.substring(proxy.indexOf(DELIMITER) + 1));
            if ((port < 80) || (port > 65_535))
            {
                return;
            }

            proxies.add(new Proxy(ipAddress, port));
        });

        return proxies.size();
    }
}