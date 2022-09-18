package org.bsoftware.parcel.utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.exceptions.EnumArgumentsValidationException;

import java.util.Arrays;

/**
 * ValidationUtility class provides various validation methods
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtility
{
    /**
     * Checks, if passed enum arguments are valid
     *
     * @param types actual enum arguments
     * @param <T> generic wildcard for particular enum
     * @throws EnumArgumentsValidationException if arguments are not valid
     */
    @SafeVarargs
    public static <T extends Enum<T>> void validateEnumArguments(T... types) throws EnumArgumentsValidationException
    {
        if ((types.length == 0) || (types.length > 2))
        {
            throw new EnumArgumentsValidationException("Types length is out on ranges");
        }
        else if (Arrays.stream(types).distinct().count() < types.length)
        {
            throw new EnumArgumentsValidationException("You can't pass the same types several times");
        }
    }
}