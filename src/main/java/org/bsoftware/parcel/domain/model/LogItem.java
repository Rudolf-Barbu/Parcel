package org.bsoftware.parcel.domain.model;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Objects;

/**
 * LogItem is a class that is used for representing log item in UI
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
@Getter
@SuppressWarnings(value = "unused")
public final class LogItem extends HBox
{
    /**
     * Container for log item's timestamp
     */
    @FXML
    private Label labelTimestamp;

    /**
     * Container for log item's message
     */
    @FXML
    private Label labelMessage;

    /**
     * Constructor which loadings UI representation from corresponding FXNL file
     */
    @SneakyThrows
    public LogItem()
    {
        final FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(LogItem.class.getResource("/fxml/model/log_item.fxml")));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
    }
}