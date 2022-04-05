package org.bsoftware.parcel.domain.model;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DataType is enum, which indicates allowed log levels
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum LogLevel
{
    FINE(Color.DARKGREEN),
    INFO(Color.DARKGRAY),
    WARNING(Color.DARKORANGE),
    ERROR(Color.DARKRED);

    /**
     * Color of corresponding log level
     */
    private final Color logColor;
}