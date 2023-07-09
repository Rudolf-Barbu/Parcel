package org.bsoftware.parcel.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.bsoftware.parcel.Parcel;
import org.bsoftware.parcel.domain.callbacks.BruteForceCallback;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.components.ThreadContainer;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.ThreadType;
import org.bsoftware.parcel.domain.runnables.BruteForceRunnable;
import org.bsoftware.parcel.utilities.FileSystemUtility;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * MainController class is used for loading menu's UI and communicating with components
 *
 * @author Rudolf Barbu
 * @version 19
 */
public class MainController implements DataLoadingCallback, BruteForceCallback
{
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
     * Loads and sends sources to service
     */
    @FXML
    public void loadSources()
    {
        FileSystemUtility.loadData(Parcel.getSCENE(), DataType.SOURCE, this);
    }

    /**
     * Loads and sends proxies to service
     */
    @FXML
    public void loadProxies()
    {
        FileSystemUtility.loadData(Parcel.getSCENE(), DataType.PROXY, this);
    }

    /**
     * Opens application folder
     */
    @FXML
    public void openApplicationFolder()
    {
        try
        {
            FileSystemUtility.openApplicationFolder();
        }
        catch (final URISyntaxException | IOException exception)
        {
            logViewLog.log(LogLevel.ERROR, String.format("Can't open application folder, message: %s", exception.getMessage()));
        }
    }

    /**
     * Sends command to service, to create and start working threads
     */
    @FXML
    public void start()
    {
        try
        {
            if ((DataContainer.isDataEmpty(DataType.SOURCE) || DataContainer.isDataEmpty(DataType.PROXY)) || ThreadContainer.isWorkStillExecuting(ThreadType.LOADING, ThreadType.BRUTEFORCE))
            {
                logViewLog.log(LogLevel.WARNING, "Load data or/and wait, until all work is interrupted");
                return;
            }

            FileSystemUtility.createWorkingDirectory();
            ThreadContainer.startBruteForceThreads(new BruteForceRunnable(this));
        }
        catch (final URISyntaxException | IOException exception)
        {
            logViewLog.log(LogLevel.ERROR, "Can't create working directory");
        }

        logViewLog.log(LogLevel.INFO, "Work started");
    }

    /**
     * Interrupts working threads
     */
    @FXML
    public void interrupt()
    {
        if (ThreadContainer.isWorkStillExecuting(ThreadType.LOADING, ThreadType.BRUTEFORCE))
        {
            logViewLog.log(LogLevel.INFO, "Work is being interrupted");

            ThreadContainer.interruptBruteforceThreads();
            return;
        }

        logViewLog.log(LogLevel.WARNING, "Work already interrupted, or not started yet");
    }

    /**
     * Clears all data
     */
    @FXML
    public void clear()
    {
        if (ThreadContainer.isWorkStillExecuting(ThreadType.LOADING, ThreadType.BRUTEFORCE))
        {
            logViewLog.log(LogLevel.WARNING, "Cannot clear data, until all work is not interrupted");
            return;
        }
        else if (DataContainer.isDataEmpty(DataType.SOURCE, DataType.PROXY))
        {
            logViewLog.log(LogLevel.WARNING, "Data containers are already empty");
            return;
        }

        clearDataAndResetCounters();
        logViewLog.log(LogLevel.INFO, "Data cleared");
    }

    /**
     * Prints message to LogView
     *
     * @param message message, which is transmitted to service
     */
    @Override
    public void handleDataLoadingMessage(final LogLevel logLevel, final String message)
    {
        Platform.runLater(() -> logViewLog.log(logLevel, message));
    }

    /**
     * Saving processed data and updating counters
     *
     * @param loadedData collection with loaded data
     * @param dataType data-type, which presented in processed data set
     */
    @Override
    public synchronized void handleLoadedData(final Collection<?> loadedData, final DataType dataType)
    {
        if (loadedData.isEmpty())
        {
            Platform.runLater(() -> logViewLog.log(LogLevel.WARNING, String.format("File with %s returned empty set", dataType.getDataTypeNameInPlural())));
            return;
        }

        if (dataType == DataType.SOURCE)
        {
            DataContainer.refreshData(DataType.SOURCE, loadedData);
            Platform.runLater(() -> labelSources.setText(String.valueOf(loadedData.size())));
        }
        else
        {
            DataContainer.refreshData(DataType.PROXY, loadedData);
            Platform.runLater(() -> labelProxies.setText(String.valueOf(loadedData.size())));
        }

        Platform.runLater(() -> logViewLog.log(LogLevel.FINE, String.format("File with %s processed", dataType.getDataTypeNameInPlural())));
    }

    /**
     * Decrements source counter
     */
    @Override
    public void handleDecrementSourcesCounter()
    {
        Platform.runLater(() -> labelSources.setText(String.valueOf(Integer.parseInt(labelSources.getText()) - 1)));
    }

    /**
     * Prints message to LogView
     *
     * @param message message, which is transmitted to service
     */
    @Override
    public void handleBruteForceMessage(final LogLevel logLevel, final String message)
    {
        Platform.runLater(() -> logViewLog.log(logLevel, message));
    }

    /**
     * Handles thread interruption, and prints log
     */
    @Override
    public synchronized void handleThreadInterruption() throws IOException
    {
        if (ThreadContainer.isTheLastBruteForceThreadLeft())
        {
            if (!DataContainer.isDataEmpty(DataType.SOURCE))
            {
                FileSystemUtility.saveRestSources();
            }

            clearDataAndResetCounters();
            Platform.runLater(() -> logViewLog.log(LogLevel.INFO, "Work interrupted"));
        }
    }

    /**
     * Clears data and resets counters
     */
    private void clearDataAndResetCounters()
    {
        DataContainer.clearData();

        Platform.runLater(() -> labelSources.setText("0"));
        Platform.runLater(() -> labelProxies.setText("0"));
    }

    /**
     * Triggered after all controls are loaded
     */
    @FXML
    private void initialize()
    {
        logViewLog.log(LogLevel.INFO, "Main UI initialized");
    }
}