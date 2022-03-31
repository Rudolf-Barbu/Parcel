package org.bsoftware.parcel.mvc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.mvc.services.MainService;

import java.io.File;
import java.util.Optional;

/**
 * MainController class is used for loading UI and communicating with service
 *
 * @author Rudolf Barbu
 * @version 1.0.0
 */
public class MainController
{
    /**
     * VBox used for opening file dialog
     */
    @FXML
    private HBox hBoxRoot;

    /**
     * Sources counter
     */
    @FXML
    private Label labelSources;

    /**
     * Proxies counter
     */
    @FXML
    private Label labelProxies;

    /**
     * Custom log container
     */
    @FXML
    private LogView logViewLog;

    /**
     * Corresponding service
     */
    private MainService mainService;

    /**
     * Loads and sends sources to service
     *
     * @param mouseEvent - event to determine affected button
     */
    @FXML
    public void loadSources(final MouseEvent mouseEvent)
    {
        final Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.SOURCE));
        final Button affectedButton = ((Button) mouseEvent.getSource());

        mainService.processData(optionalFile, DataType.SOURCE, affectedButton);
    }

    /**
     * Loads and sends proxies to service
     *
     * @param mouseEvent - event to determine affected button
     */
    @FXML
    public void loadProxies(final MouseEvent mouseEvent)
    {
        final Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.PROXY));
        final Button affectedButton = ((Button) mouseEvent.getSource());

        mainService.processData(optionalFile, DataType.PROXY, affectedButton);
    }

    /**
     * Shows window, so user can select file
     *
     * @param dataType - type of data, used for determine window's title
     * @return path to selected file, null if operation cancelled
     */
    private File loadData(final DataType dataType)
    {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(String.format("Load %s", dataType.getDataTypeNameInPlural()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return fileChooser.showOpenDialog(hBoxRoot.getScene().getWindow());
    }

    /**
     * Triggered after all controls are loaded
     */
    @FXML
    private void initialize()
    {
        mainService = new MainService(labelSources, labelProxies, logViewLog);
        logViewLog.info("Application initialized");
    }
}