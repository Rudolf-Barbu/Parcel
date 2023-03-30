package org.bsoftware.parcel.domain.model;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LogLevel is an enum with all supported log levels
 *
 * @author Rudolf Barbu
 * @version 2
 */
@RequiredArgsConstructor
public enum LogLevel
{
    FINE(Color.DARKGREEN), INFO(Color.DARKGRAY), WARNING(Color.DARKORANGE), ERROR(Color.DARKRED);

    /**
     * Color of corresponding log level
     */
    @Getter
    private final Color logColor;
}