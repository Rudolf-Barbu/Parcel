package org.bsoftware.parcel.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import org.bsoftware.parcel.Parcel;
import org.bsoftware.parcel.utilities.OperatingSystemUtility;

import java.util.Optional;

/**
 * StackController class is used for loading and managing application UI
 *
 * @author Rudolf Barbu
 * @version 1
 */
public class StackController
{
    /**
     * Defines application title pattern
     */
    private static final String TITLE_PATTERN = "(%s | %s)";

    /**
     * Part of the title with description
     */
    @FXML
    private Label labelDescription;

    /**
     * Offset of application window, on X axis
     */
    private double windowOffsetX;

    /**
     * Offset of application window, on Y axis
     */
    private double windowOffsetY;

    /**
     * Drags application window, using current position and window offset
     *
     * @param mouseEvent passed node event
     */
    @FXML
    public void dragApplicationWindow(final MouseEvent mouseEvent)
    {
        final Window window = Parcel.getSCENE().getWindow();

        if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED)
        {
            windowOffsetX = (window.getX() - mouseEvent.getScreenX());
            windowOffsetY = (window.getY() - mouseEvent.getScreenY());
        }
        else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED)
        {
            window.setX(mouseEvent.getScreenX() + windowOffsetX);
            window.setY(mouseEvent.getScreenY() + windowOffsetY);
        }
    }

    /**
     * Closes the application
     */
    @FXML
    public void closeApplication()
    {
        Platform.exit();
    }

    /**
     * Triggered after all controls are loaded
     */
    @FXML
    private void initialize()
    {
        labelDescription.setText(String.format(TITLE_PATTERN, Optional.ofNullable(StackController.class.getPackage().getImplementationVersion()).orElse("Developer mode"), OperatingSystemUtility.getBIT_DEPTH()));
    }
}