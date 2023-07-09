package org.bsoftware.parcel;

import com.chilkatsoft.CkGlobal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.bsoftware.parcel.domain.exceptions.ApplicationInitializationException;
import org.bsoftware.parcel.utilities.OperatingSystemUtility;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;

/**
 * Parcel is a class, which load native binary and starts the JavaFX application
 *
 * @author Rudolf Barbu
 * @version 8
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
        final Properties properties = new Properties();

        try (final InputStream inputStream = Parcel.class.getResourceAsStream(String.format("/binaries/%s/chilkat.%s", OperatingSystemUtility.getBIT_DEPTH(), OperatingSystemUtility.getBinaryExtension())))
        {
            properties.load(Parcel.class.getResourceAsStream("/application.properties"));

            Files.copy(Objects.requireNonNull(inputStream), path, StandardCopyOption.REPLACE_EXISTING);
            System.load(path.toString());

            SCENE = new Scene(FXMLLoader.load(Objects.requireNonNull(Parcel.class.getResource("/fxml/views/stack_view.fxml"))));
        }
        catch (final IOException ioException)
        {
            throw new ApplicationInitializationException("I/O exception occurred, message: %s", ioException.getMessage());
        }

        unlockBinary(properties.getProperty("application.key"));
    }

    /**
     * Defines application scene
     */
    @Getter
    private static final Scene SCENE;

    /**
     * Defines application icon
     */
    private static final Image ICON = new Image(Objects.requireNonNull(Parcel.class.getResourceAsStream("/static/images/icon.png")));

    /**
     * Unlocks loaded binary, using license key
     *
     * @throws ApplicationInitializationException if license key is invalid or expired
     */
    private static void unlockBinary(final String applicationKey) throws ApplicationInitializationException
    {
        final CkGlobal ckGlobal = new CkGlobal();

        if ((ckGlobal.UnlockBundle(applicationKey)) && (ckGlobal.get_UnlockStatus() != 2))
        {
            throw new ApplicationInitializationException("Can't unlock binary, reason: %s", ckGlobal.lastErrorText());
        }
    }

    /**
     * Entry point of the application
     *
     * @param arguments parameters passed though the command line, if presents
     */
    public static void main(final String... arguments)
    {
        launch(arguments);
    }

    /**
     * Setups the application window
     *
     * @param stage main stage of the application
     */
    @Override
    public void start(final Stage stage)
    {
        stage.getIcons().add(ICON);
        stage.setResizable(Boolean.FALSE);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(SCENE);

        stage.show();
    }
}