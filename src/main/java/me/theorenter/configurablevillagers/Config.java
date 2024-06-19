package me.theorenter.configurablevillagers;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
/**
 * Configuration of the {@link ConfigurableVillagers}.
 */
public final class Config {

    public final String DEFAULT_LOCALIZATION;
    public final boolean LOCALIZATION_CLIENT_ORIENTED;
    public final boolean USE_CUSTOM_TRADES;
    public final boolean USE_VANILLA_CARTOGRAPHER_MAPS;
    public final boolean MAJOR_POSITIVE_FIXED;
    public final int MAJOR_POSITIVE_VALUE;
    public final boolean MINOR_POSITIVE_FIXED;
    public final int MINOR_POSITIVE_VALUE;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Config(@NotNull ConfigurableVillagers plugin) throws IOException, InvalidConfigurationException {
        File file;
        FileConfiguration fileConfig;

        file = new File(plugin.getDataFolder() + File.separator + "settings",
                "configuration.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("settings" + File.separator + "configuration.yml", false);
        }

        fileConfig = new YamlConfiguration();
        fileConfig.load(file);

        DEFAULT_LOCALIZATION = fileConfig.getString("language.default-localization");
        LOCALIZATION_CLIENT_ORIENTED = fileConfig.getBoolean("language.client-based");
        USE_CUSTOM_TRADES = fileConfig.getBoolean("settings.use-custom-trades");
        USE_VANILLA_CARTOGRAPHER_MAPS = fileConfig.getBoolean("settings.use-vanilla-cartographer-maps");
        MAJOR_POSITIVE_FIXED = fileConfig.getBoolean("settings.curing.major-positive.fixed");
        MAJOR_POSITIVE_VALUE = fileConfig.getInt("settings.curing.major-positive.set");
        MINOR_POSITIVE_FIXED = fileConfig.getBoolean("settings.curing.minor-positive.fixed");
        MINOR_POSITIVE_VALUE = fileConfig.getInt("settings.curing.minor-positive.set");
    }
}
