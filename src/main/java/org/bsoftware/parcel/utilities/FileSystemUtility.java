package org.bsoftware.parcel.utilities;

import javafx.scene.Node;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.components.LogView;
import org.bsoftware.parcel.domain.components.ThreadContainer;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.Source;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FileSystemUtility class provides various file system operations
 *
 * @author Rudolf Barbu
 * @version 1.0.2
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileSystemUtility
{
    /**
     * Defines title for file dialog
     */
    private static final String TITLE_PATTERN = "Load %s file";

    /**
     * Defines file extension pattern
     */
    private static final String EXTENSION_PATTERN = "*.txt";

    /**
     * Path to working directory
     */
    private static Path workingDirectory;

    /**
     * Loads data, corresponds data-type
     *
     * @param node - root element for window retrieving
     * @param dataType - particular datatype
     * @param dataLoadingCallback - callback
     */
    public static void loadData(final Node node, final DataType dataType, final DataLoadingCallback dataLoadingCallback)
    {
        final Optional<File> optionalFile = loadTextFile(node, dataType);

        if (optionalFile.isPresent())
        {
            if (ThreadContainer.isWorkStillExecuting(ThreadContainer.WorkType.LOADING, ThreadContainer.WorkType.BRUTEFORCE))
            {
                dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.WARNING, "Cannot run two processing tasks or/and brute-force in parallel");
                return;
            }

            ThreadContainer.startLoadingThread(optionalFile.get(), dataType, dataLoadingCallback);
        }
        else
        {
            dataLoadingCallback.handleDataLoadingMessage(LogView.LogLevel.WARNING, "Operation cancelled by user");
        }
    }

    /**
     * Creates and sets working directory
     */
    public static void createWorkingDirectory() throws URISyntaxException, IOException
    {
        workingDirectory = Files.createDirectory(Paths.get(FileSystemUtility.class.getProtectionDomain().getCodeSource().getLocation().toURI()).resolve(String.format("../results [%s]", OperatingSystemUtility.getFormattedCurrentTime())));
    }

    /**
     * Saves source to file
     *
     * @param fileName destination file name
     * @param lineParts additional parts of the line
     * @throws IOException if method can't create file or directory
     */
    public static synchronized void saveSourceToFile(final String fileName, final Source source, final String... lineParts) throws IOException
    {
        if (workingDirectory == null)
        {
            throw new IOException("Working directory cannot be null");
        }

        final Path pathToFile = workingDirectory.resolve(String.format("%s.txt", fileName));
        final String sourcePrefix = String.format("%s:%s", source.getCredential(), source.getPassword());
        final String sourcePostfix = Arrays.stream(lineParts).map(linePart -> String.format(" | %s", linePart)).collect(Collectors.joining());

        try (final AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(pathToFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
        {
            asynchronousFileChannel.write(ByteBuffer.wrap(sourcePrefix.concat(sourcePostfix).concat(System.lineSeparator()).getBytes()), asynchronousFileChannel.size());
        }
    }

    /**
     * Allows to user to choose a text file
     *
     * @param node parent element of current, active window
     * @return path to selected file, null if operation cancelled
     */
    private static Optional<File> loadTextFile(final Node node, final DataType dataType)
    {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(String.format(TITLE_PATTERN, dataType.getDataTypeNameInPlural()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", EXTENSION_PATTERN));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return Optional.ofNullable(fileChooser.showOpenDialog(node.getScene().getWindow()));
    }
}