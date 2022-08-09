package org.bsoftware.parcel.utilities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.exceptions.OperatingSystemNotSupportedException;
import oshi.SystemInfo;
import oshi.software.common.AbstractOperatingSystem;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * OperatingSystemUtilities class is used for recognition of operating system
 *
 * @author Rudolf Barbu
 * @version 1.0.4
 */
@SuppressWarnings("DanglingJavadoc")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperatingSystemUtility
{
    /**
     * This static block checks, if operating system is supported
     */
    static
    {
        final SystemInfo systemInfo = new SystemInfo();
        final AbstractOperatingSystem abstractOperatingSystem = (AbstractOperatingSystem) systemInfo.getOperatingSystem();

        OPERATING_SYSTEM = Arrays.stream(OperatingSystem.values()).filter(operatingSystem -> abstractOperatingSystem.getFamily().equals(operatingSystem.matchingCriteria)).findAny().orElseThrow(() -> new OperatingSystemNotSupportedException(abstractOperatingSystem.getFamily()));
        BIT_DEPTH = (abstractOperatingSystem.getBitness() == 32) ? "x86" : "x64";
    }

    /**
     * Defines container for recognized operating system
     */
    private static final OperatingSystem OPERATING_SYSTEM;

    /**
     * Defines container and getter for operating system's bit depth
     */
    @Getter
    private static final String BIT_DEPTH;

    /**
     * Getter for binary file extension
     *
     * @return extension, depends on a current operating system
     */
    public static String getBinaryExtension()
    {
        return OPERATING_SYSTEM.binaryExtension;
    }

    /**
     * Method to get current time
     *
     * @return properly-formatted current time
     */
    public static String getFormattedCurrentTime()
    {
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME).replace(':', '.');
    }

    /**
     * Enum with all supported operating systems
     */
    @RequiredArgsConstructor
    private enum OperatingSystem
    {
        WINDOWS("Windows", "dll"), LINUX("Linux", "so");

        /**
         * Keyword for matching the operating system
         */
        private final String matchingCriteria;

        /**
         * Extension for binary file
         */
        private final String binaryExtension;
    }
}