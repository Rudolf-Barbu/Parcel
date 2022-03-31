package org.bsoftware.parcel.mvc.services;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataProcessingCallback;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Proxy;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.domain.runnables.DataProcessingRunnable;
import org.bsoftware.parcel.utilities.DataContainerUtility;

import java.io.File;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class MainService implements DataProcessingCallback
{
    private static final ExecutorService DATA_PROCESSING_EXECUTORS_SERVICE = Executors.newFixedThreadPool(2);

    private static final EnumMap<DataType, CompletableFuture<Void>> DATA_PROCESSING_COMPLETABLE_FUTURE_MAP = new EnumMap<>(DataType.class);

    private final Label labelSources;

    private final Label labelProxies;

    private final LogView logViewLog;

    @SuppressWarnings(value = "OptionalUsedAsFieldOrParameterType")
    public void processData(final Optional<File> optionalFile, final DataType dataType, final Button affectedButton)
    {
        if (optionalFile.isPresent())
        {
            final CompletableFuture<Void> completableFuture = DATA_PROCESSING_COMPLETABLE_FUTURE_MAP.get(dataType);

            if ((completableFuture == null) || completableFuture.isDone())
            {
                final DataProcessingRunnable dataProcessingRunnable = new DataProcessingRunnable(optionalFile.get(), dataType, this);

                affectedButton.setDisable(true);
                DATA_PROCESSING_COMPLETABLE_FUTURE_MAP.put(dataType, CompletableFuture.runAsync(dataProcessingRunnable, DATA_PROCESSING_EXECUTORS_SERVICE).whenComplete((action, throwable) ->
                {
                    affectedButton.setDisable(false);

                    if (throwable != null)
                    {
                        Platform.runLater(() -> logViewLog.error(throwable.getCause().getMessage()));
                    }
                }));
            }
            else
            {
                logViewLog.warning("Cannot run two same tasks in parallel");
            }
        }
        else
        {
            logViewLog.warning("Operation cancelled by user");
        }
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public void handleProcessedData(final HashSet<?> processedData, final DataType dataType, final long elapsedTimeInMilliseconds)
    {
        if (processedData.isEmpty())
        {
            Platform.runLater(() -> logViewLog.warning(String.format("File with %s returned empty set", dataType.getDataTypeNameInPlural())));
            return;
        }

        if (dataType == DataType.SOURCE)
        {
            DataContainerUtility.refreshSources((HashSet<Source>) processedData);
            Platform.runLater(() -> labelSources.setText(String.valueOf(processedData.size())));
        }
        else if (dataType == DataType.PROXY)
        {
            DataContainerUtility.refreshProxies((HashSet<Proxy>) processedData);
            Platform.runLater(() -> labelProxies.setText(String.valueOf(processedData.size())));
        }

        Platform.runLater(() -> logViewLog.fine(String.format("File with %s processed in %d ms", dataType.getDataTypeNameInPlural(), elapsedTimeInMilliseconds)));
    }
}