package org.bsoftware.parcel.domain.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;
import org.bsoftware.parcel.domain.model.LogItem;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.utilities.OperatingSystemUtility;

import java.util.Objects;

/**
 * LogView is a class that is used for representing log section in UI
 *
 * @author Rudolf Barbu
 * @version 10
 */
@SuppressWarnings("java:S110")
public class LogView extends ScrollPane
{
    /**
     * Defines path to log view FXML
     */
    private static final String FXML = "/fxml/components/log_view.fxml";

    /**
     * Defines max allowed log items
     */
    private static final short MAX_LOG_ITEMS = 256;

    /**
     * Defines boundary vertical value, used for auto-scroll trigger
     */
    public static final double BOUNDARY_VERTICAL_VALUE = 0.9D;

    /**
     * Container for log items
     */
    @FXML
    private VBox vBoxLogItemContainer;

    /**
     * Adds new log item, with log level
     *
     * @param logLevel color, that represents log level
     * @param logMessage particular log message
     */
    public void log(final LogLevel logLevel, final String logMessage)
    {
        final LogItem logItem = new LogItem();

        logItem.getLabelTimestamp().setText(String.format("[%s]", OperatingSystemUtility.getFormattedCurrentTime()));
        logItem.getLabelMessage().setText(logMessage);
        logItem.getLabelMessage().setTextFill(logLevel.getLogColor());

        truncateLog();
        vBoxLogItemContainer.getChildren().add(logItem);

        updateLayoutAndScroll();
    }

    /**
     * Prevents to the LogView object to have focus
     */
    @Override
    public void requestFocus()
    {
        setFocused(Boolean.FALSE);
    }

    /**
     * Truncating log container, then limit of items is excited
     */
    private void truncateLog()
    {
        if (vBoxLogItemContainer.getChildren().size() >= MAX_LOG_ITEMS)
        {
            vBoxLogItemContainer.getChildren().remove(0);
        }
    }

    /**
     * Updates the layout of the container and auto-scrolls it to the bottom
     */
    private void updateLayoutAndScroll()
    {
        if (getVvalue() > BOUNDARY_VERTICAL_VALUE)
        {
            applyCss();
            layout();
            setVvalue(getVmax());
        }
    }

    /**
     * Constructor which loadings UI representation from corresponding FXML file
     */
    @SneakyThrows
    public LogView()
    {
        final FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(LogView.class.getResource(FXML)));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
    }
}