package org.bsoftware.parcel.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * ValidationUtility class provides various validation methods
 *
 * @author Rudolf Barbu
 * @version 2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtility
{
    /**
     * Checks, if passed enum arguments are valid
     *
     * @param types actual enum arguments
     * @param <T> generic wildcard for particular enum
     * @throws IllegalArgumentException if arguments are not valid
     */
    @SafeVarargs
    public static <T extends Enum<T>> void validateEnumArguments(T... types)
    {
        if ((types.length == 0) || (types.length > 2))
        {
            throw new IllegalArgumentException("Types length is out on ranges");
        }
        else if (Arrays.stream(types).distinct().count() < types.length)
        {
            throw new IllegalArgumentException("You can't pass the same type several times");
        }
    }
}