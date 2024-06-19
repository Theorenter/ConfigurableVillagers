package me.theorenter.configurablevillagers.localization;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.PropertyResourceBundle;

/**
 * The localization set.
 */
public class LocalizationBundle {
    private final PropertyResourceBundle locProperty;

    /**
     * @param bundleFile The localization file.
     * @throws IOException if an error occurred when reading from the input stream.
     */
    public LocalizationBundle(@NotNull final File bundleFile) throws IOException {
        this.locProperty = new PropertyResourceBundle(Files.newInputStream(bundleFile.toPath()));
    }

    /**
     * @param key The key of the string.
     * @return The string of localization by the key.
     */
    public String getRaw(@NotNull final String key) {
        return locProperty.getString(key);
    }
}
