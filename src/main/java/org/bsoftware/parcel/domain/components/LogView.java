package org.bsoftware.parcel.domain.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.SneakyThrows;
import org.bsoftware.parcel.domain.model.LogItem;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * LogView is a class that is used for representing log section in UI
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
public class LogView extends ScrollPane
{
    /**
     * Max allowed log items
     */
    private static final short MAX_LOG_ITEMS = 4_096;

    /**
     * Container for log items
     */
    @FXML
    private VBox vBoxLogItemContainer;

    /**
     * Adds new log item, with FINE log level
     *
     * @param message - log message
     */
    public final void fine(final String message)
    {
        addLogItem(message, Color.DARKGREEN);
    }

    /**
     *  Adds new log item, with INFO log level
     *
     * @param message - log message
     */
    public final void info(final String message)
    {
        addLogItem(message, Color.DARKGRAY);
    }

    /**
     *  Adds new log item, with WARNING log level
     *
     * @param message - log message
     */
    public final void warning(final String message)
    {
        addLogItem(message, Color.DARKORANGE);
    }

    /**
     *  Adds new log item, with ERROR log level
     *
     * @param message - log message
     */
    public final void error(final String message)
    {
        addLogItem(message, Color.DARKRED);
    }

    /**
     * Prevents to the LogView object to have focus
     */
    @Override
    public void requestFocus()
    {
        setFocused(false);
    }

    /**
     * Particular implementation of adding new log item to VBox container
     *
     * @param message - log message
     * @param messageTextColor - color, that represents log level
     */
    private void addLogItem(final String message, final Color messageTextColor)
    {
        final LogItem logItem = new LogItem();

        logItem.getLabelTimestamp().setText(String.format("[%s]", LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME)));
        logItem.getLabelMessage().setText(message);
        logItem.getLabelMessage().setTextFill(messageTextColor);

        truncateLog();
        vBoxLogItemContainer.getChildren().add(logItem);

        updateLayoutAndScroll();
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
        if (getVvalue() > 0.9D)
        {
            applyCss();
            layout();
            setVvalue(getVmax());
        }
    }

    /**
     * Constructor which loadings UI representation from corresponding FXNL file
     */
    @SneakyThrows
    public LogView()
    {
        final FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(LogView.class.getResource("/fxml/components/log_view.fxml")));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        fxmlLoader.load();
    }
}