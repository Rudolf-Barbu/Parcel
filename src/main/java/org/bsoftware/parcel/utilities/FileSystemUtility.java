package org.bsoftware.parcel.utilities;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bsoftware.parcel.domain.callbacks.DataLoadingCallback;
import org.bsoftware.parcel.domain.components.DataContainer;
import org.bsoftware.parcel.domain.components.ThreadContainer;
import org.bsoftware.parcel.domain.model.DataType;
import org.bsoftware.parcel.domain.model.LogLevel;
import org.bsoftware.parcel.domain.model.Source;
import org.bsoftware.parcel.domain.model.ThreadType;

import java.awt.*;
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
 * FileSystemUtility class provides various file system methods
 *
 * @author Rudolf Barbu
 * @version 10
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
     * Defines file name for rest sources
     */
    private static final String REST_FILE_NAME = "rest.txt";

    /**
     * Path to working directory
     */
    private static Path workingDirectory;

    /**
     * Loads data, corresponds data-type
     *
     * @param scene scene object for window retrieving
     * @param dataType particular datatype
     * @param dataLoadingCallback callback
     */
    public static void loadData(final Scene scene, final DataType dataType, final DataLoadingCallback dataLoadingCallback)
    {
        final Optional<File> optionalFile = loadTextFile(scene, dataType);

        if (optionalFile.isPresent())
        {
            if (ThreadContainer.isWorkStillExecuting(ThreadType.LOADING, ThreadType.BRUTEFORCE))
            {
                dataLoadingCallback.handleDataLoadingMessage(LogLevel.WARNING, "Cannot run two processing tasks or/and brute-force in parallel");
                return;
            }

            ThreadContainer.startLoadingThread(optionalFile.get(), dataType, dataLoadingCallback);
        }
        else
        {
            dataLoadingCallback.handleDataLoadingMessage(LogLevel.WARNING, "Operation cancelled by user");
        }
    }

    /**
     * Opens application folder
     *
     * @throws URISyntaxException if application can't convert URI to Path
     * @throws IOException if folder can't be opened in explorer
     */
    public static void openApplicationFolder() throws URISyntaxException, IOException
    {
        final File applicationFilePath = getApplicationPath().toFile();

        Desktop.getDesktop().open(applicationFilePath.isFile() ? applicationFilePath.getParentFile() : applicationFilePath);
    }

    /**
     * Creates and sets working directory
     */
    public static void createWorkingDirectory() throws URISyntaxException, IOException
    {
        workingDirectory = Files.createDirectory(getApplicationPath()).resolve(String.format("../results [%s]", OperatingSystemUtility.getFormattedCurrentTime()));
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

        asynchronousWrite(ByteBuffer.wrap(sourcePrefix.concat(sourcePostfix).concat(System.lineSeparator()).getBytes()), pathToFile);
    }

    /**
     * Saves the rest of sources
     *
     * @throws IOException if method can't create file or directory
     */
    public static void saveRestSources() throws IOException
    {
        if (workingDirectory == null)
        {
            throw new IOException("Working directory cannot be null");
        }

        asynchronousWrite(DataContainer.getSourcesByteBuffer(), workingDirectory.resolve(REST_FILE_NAME));
    }

    /**
     * Gets application path, based on it's location
     *
     * @return application path
     * @throws URISyntaxException if application can't convert URI to Path
     */
    private static Path getApplicationPath() throws URISyntaxException
    {
        return Paths.get(FileSystemUtility.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    /**
     * Allows to user to choose a text file
     *
     * @param scene scene object for window retrieving
     * @return path to selected file, null if operation cancelled
     */
    private static Optional<File> loadTextFile(final Scene scene, final DataType dataType)
    {
        final FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(String.format(TITLE_PATTERN, dataType.getDataTypeNameInPlural()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", EXTENSION_PATTERN));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        return Optional.ofNullable(fileChooser.showOpenDialog(scene.getWindow()));
    }

    /**
     * Writes byte buffer asynchronously
     *
     * @param byteBuffer buffer to save
     * @param pathToFile path to target file
     * @throws IOException if method can't save byte buffer
     */
    private static void asynchronousWrite(final ByteBuffer byteBuffer, final Path pathToFile) throws IOException
    {
        try (final AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(pathToFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
        {
            asynchronousFileChannel.write(byteBuffer, asynchronousFileChannel.size());
        }
    }
}