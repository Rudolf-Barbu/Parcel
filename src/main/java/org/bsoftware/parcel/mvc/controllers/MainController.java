package org.bsoftware.parcel.mvc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.exceptions.DataProcessingException;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.mvc.services.MainService;

import java.io.File;
import java.util.Optional;

public class MainController
{
    private final MainService mainService = MainService.getINSTANCE();

    @FXML
    private HBox hBoxRoot;

    @FXML
    private Label labelSources;

    @FXML
    private Label labelProxies;

    @FXML
    private LogView logViewLog;

    @FXML
    public void loadSources()
    {
        Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.SOURCE));
        sendDataToService(optionalFile, DataType.SOURCE);
    }

    @FXML
    public void loadProxies()
    {
        Optional<File> optionalFile = Optional.ofNullable(loadData(DataType.PROXY));
        sendDataToService(optionalFile, DataType.PROXY);
    }

    private File loadData(final DataType dataType)
    {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(String.format("Load %s", dataType.getDataTypeNameInPlural()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return fileChooser.showOpenDialog(hBoxRoot.getScene().getWindow());
    }

    @SuppressWarnings(value = "OptionalUsedAsFieldOrParameterType")
    private void sendDataToService(final Optional<File> optionalFile, final DataType dataType)
    {
        if (optionalFile.isPresent())
        {
            try
            {
                final long pointcut = System.nanoTime();
                final int processedLines = mainService.processData(optionalFile.get(), dataType);

                if (dataType == DataType.SOURCE)
                {
                    labelSources.setText(String.valueOf(processedLines));
                }
                else if (dataType == DataType.PROXY)
                {
                    labelProxies.setText(String.valueOf(processedLines));
                }

                logViewLog.fine(String.format("File with %s processed in %d ms", dataType.getDataTypeNameInPlural(), (System.nanoTime() - pointcut) / 1_000_000));
            }
            catch (DataProcessingException dataProcessingException)
            {
                logViewLog.error(dataProcessingException.getMessage());
            }

            return;
        }

        logViewLog.warning("Operation cancelled by user");
    }

    @FXML
    private void initialize()
    {
        logViewLog.info("Application initialized");
    }
}