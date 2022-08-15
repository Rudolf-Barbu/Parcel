package org.bsoftware.parcel;

import com.chilkatsoft.CkGlobal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bsoftware.parcel.domain.exceptions.BinaryFileException;
import org.bsoftware.parcel.utilities.OperatingSystemUtility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

/**
 * Parcel is a class, which load native binary and starts the JavaFX application
 *
 * @author Rudolf Barbu
 * @version 1.0.3
 */
@SuppressWarnings("DanglingJavadoc")
public final class Parcel extends Application
{
    /**
     * This static initialization used for loading and unlocking binary
     */
    static
    {
        final Path path = Paths.get(System.getProperty("java.io.tmpdir"), String.format("chilkat.%s", OperatingSystemUtility.getBinaryExtension()));

        try (final InputStream inputStream = Parcel.class.getResourceAsStream(String.format("/binaries/%s/chilkat.%s", OperatingSystemUtility.getBIT_DEPTH(), OperatingSystemUtility.getBinaryExtension())))
        {
            Files.copy(Objects.requireNonNull(inputStream), path, StandardCopyOption.REPLACE_EXISTING);
            System.load(path.toString());
        }
        catch (final IOException ioException)
        {
            throw new BinaryFileException("Can't copy binary file in temporary directory, message: %s", ioException.getMessage());
        }

        unlockBinary();
    }

    /**
     * Defines path to main view FXML
     */
    private static final String FXML = "/fxml/views/main_view.fxml";

    /**
     * Defines application icon
     */
    private static final Image ICON = new Image(Objects.requireNonNull(Parcel.class.getResourceAsStream("/static/images/icon.png")));

    /**
     * Defines computed application title
     */
    private static final String TITLE = String.format("Parcel (%s | %s)", Optional.ofNullable(Parcel.class.getPackage().getImplementationVersion()).orElse("Developer mode"), OperatingSystemUtility.getBIT_DEPTH());

    /**
     * Defines licence key for binary
     */
    private static final String UNLOCK_KEY = "3o3UnK.CBX0926_sYR54NwIB0nb";

    /**
     * Entry point of the application
     *
     * @param arguments - parameters passed though the command line, if presents
     */
    public static void main(final String... arguments)
    {
        launch(arguments);
    }

    /**
     * Setups the application window
     *
     * @param stage - main stage of the application
     * @throws IOException, if application can't load the FXML file
     */
    @Override
    public void start(final Stage stage) throws IOException
    {
        final Scene scene = new Scene(FXMLLoader.load(Objects.requireNonNull(Parcel.class.getResource(FXML))));

        stage.getIcons().add(ICON);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.setResizable(Boolean.FALSE);

        stage.show();
    }

    /**
     * Unlocks loaded binary, using license key
     *
     * @throws BinaryFileException, if license key is invalid or expired
     */
    private static void unlockBinary() throws BinaryFileException
    {
        final CkGlobal ckGlobal = new CkGlobal();

        if ((ckGlobal.UnlockBundle(UNLOCK_KEY)) && (ckGlobal.get_UnlockStatus() != 2))
        {
            throw new BinaryFileException("Can't unlock binary, reason: %s", ckGlobal.lastErrorText());
        }
    }
}