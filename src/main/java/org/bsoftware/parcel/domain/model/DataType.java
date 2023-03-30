package org.bsoftware.parcel.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DataType is enum, which indicates allowed data-types for controller and service classes
 *
 * @author Rudolf Barbu
 * @version 3
 */
@RequiredArgsConstructor
public enum DataType
{
    SOURCE("sources"), PROXY("proxies");

    /**
     * Verbal representation of the data-type, in plural
     */
    @Getter
    private final String dataTypeNameInPlural;
}