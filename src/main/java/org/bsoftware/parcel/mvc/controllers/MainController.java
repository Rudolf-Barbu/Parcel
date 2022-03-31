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

public class MainController
{
    @FXML
    private HBox hBoxRoot;

    @FXML
    private Label labelSources;

    @FXML
    private Label labelProxies;

    @FXML
    private LogView logViewLog;

    private MainService mainService;

    @FXML
    public void loadSources(final MouseEvent mouseEvent)
    {
        final Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.SOURCE));
        final Button affectedButton = ((Button) mouseEvent.getSource());

        mainService.processData(optionalFile, DataType.SOURCE, affectedButton);
    }

    @FXML
    public void loadProxies(final MouseEvent mouseEvent)
    {
        final Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.PROXY));
        final Button affectedButton = ((Button) mouseEvent.getSource());

        mainService.processData(optionalFile, DataType.PROXY, affectedButton);
    }

    private File loadData(final DataType dataType)
    {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(String.format("Load %s", dataType.getDataTypeNameInPlural()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return fileChooser.showOpenDialog(hBoxRoot.getScene().getWindow());
    }

    @FXML
    private void initialize()
    {
        mainService = new MainService(labelSources, labelProxies, logViewLog);
        logViewLog.info("Application initialized");
    }
}