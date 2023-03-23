package org.bsoftware.parcel.domain.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ProxyRatingAdjustmentType is enum, which adjustment operation upon proxy rating
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@RequiredArgsConstructor
public enum ProxyRatingAdjustmentType
{
    INCREASE(+1), DECREASE(-1);

    /**
     * Proxy rating adjustment value
     */
    @Getter
    private final int proxyRatingAdjustmentValue;
}