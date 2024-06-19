package me.theorenter.configurablevillagers.localization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Localization system of the plugin.
 */
public final class Localization {

    private final Map<String, LocalizationBundle> REFERENCE_LOCALIZATION_MAP = new HashMap<>();
    private final Map<String, LocalizationBundle> OVERRIDE_LOCALIZATION_MAP = new HashMap<>();

    private final String defaultLocaleTag;

    private final Plugin plugin;
    private final boolean isClientOriented;

    /**
     * Setups plugin's localization.
     *
     * @param plugin The plugin for which localization is created.
     * @param isClientOriented Will users receive messages from the plugin depending on their language or will they receive default plugin locale messages.
     * @param path The path to the localization files directory <b>NOTE: Without specified plugin's data folder and directory name</b>.
     * @param dir The directory name of the localization files.
     * @param defaultLocaleTag The tag of the default plugin localization.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Localization(@NotNull final Plugin plugin,
                             boolean isClientOriented,
                             @NotNull final String path,
                             @NotNull final String dir,
                             @NotNull final String defaultLocaleTag)
            throws IOException, InvalidConfigurationException {

        this.plugin = plugin;
        this.isClientOriented = isClientOriented;
        this.defaultLocaleTag = defaultLocaleTag;

        File dFolder = plugin.getDataFolder();

        String rPath, oPath;
        rPath = dFolder + File.separator + path + File.separator + dir;
        oPath = dFolder + File.separator + path + File.separator + "override";

        File rFile = new File(rPath);
        File oFile = new File(oPath);

        rFile.mkdirs();
        oFile.mkdirs();

        loadToDataFolder(path, dir);
        loadReference(rFile);
        loadOverride(oFile);
    }

    /**
     * Loads all plugin's localization to dataFolder
     */
    private void loadToDataFolder(@NotNull final String locPath, @NotNull final String locDir) throws IOException {
        Matcher m = Pattern.compile("plugins/.+\\.jar$")
                .matcher(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        String plPath = null;
        while (m.find())
            plPath = m.group(0);

        assert plPath != null;
        try (ZipFile pJAR = new ZipFile(plPath)) {
            String p = (locPath.replaceAll("\\\\", "\\/")) + "/" + locDir;
            Pattern m2 = Pattern.compile("^"+ p +"/.+$");

            Enumeration<? extends ZipEntry> zipEntries = pJAR.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = zipEntries.nextElement().getName();
                if (m2.matcher(fileName).find())
                    plugin.saveResource(fileName, true);
            }
        }
    }

    /**
     * @param dir The directory of the localization references.
     * @throws IOException if an error occurred when reading from the input stream.
     */
    @SuppressWarnings("ConstantConditions")
    private void loadReference(@NotNull final File dir) throws IOException {
        if (dir.listFiles() == null || dir.listFiles().length == 0)
            return;

        for (File bundleFile : dir.listFiles()) {
            LocalizationBundle locBundle = new LocalizationBundle(bundleFile);
            REFERENCE_LOCALIZATION_MAP.put(locBundle.getRaw("locale.tag"), locBundle);
        }
    }

    /**
     * Loads override localization from plugin's data folder.
     *
     * @param dir The directory of the localization references.
     * @throws IOException if an error occurred when reading from the input stream.
     */
    @SuppressWarnings("ConstantConditions")
    private void loadOverride(@NotNull final File dir) throws IOException {
        if (dir.listFiles() == null || dir.listFiles().length == 0)
            return;

        plugin.getLogger().info("Override localizations detected:");
        for (File bundleFile : dir.listFiles()) {
            LocalizationBundle locBundle = new LocalizationBundle(bundleFile);
            String localeTag = locBundle.getRaw("locale.tag");
            OVERRIDE_LOCALIZATION_MAP.put(localeTag, locBundle);
            plugin.getLogger().info(localeTag + " – " + locBundle.getRaw("locale.name") + " by " + locBundle.getRaw("author"));
        }
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The locale tag.
     * @return The {@link String} of the localization by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     */
    private String getClientOriented(@NotNull final String key, @NotNull final Locale localeTag) {
        // Override-client-oriented? -> Reference-client-oriented? -> Reference-default? -> key
        LocalizationBundle bundle;
        String msg;

        bundle = OVERRIDE_LOCALIZATION_MAP.get(localeTag.toString());
        if (bundle != null) {
            msg = bundle.getRaw(key);
            if (msg != null)
                return msg;
        }
        bundle = REFERENCE_LOCALIZATION_MAP.get(localeTag.toString());
        if (bundle != null) {
            msg = bundle.getRaw(key);
            if (msg != null)
                return msg;
        }
        bundle = REFERENCE_LOCALIZATION_MAP.get(defaultLocaleTag);
        if (bundle != null) {
            msg = bundle.getRaw(key);
            if (msg != null)
                return msg;
        }
        return key;
    }

    /**
     * @param key The key of the localized message.
     * @return The {@link String} of the localization by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     */
    public String getRaw(@NotNull final String key) {
        // Override-default? -> Reference-default? -> key
        LocalizationBundle bundle;
        String msg;

        bundle = OVERRIDE_LOCALIZATION_MAP.get(defaultLocaleTag);
        if (bundle != null) {
            msg = bundle.getRaw(key);
            if (msg != null)
                return msg;
        }
        bundle = REFERENCE_LOCALIZATION_MAP.get(defaultLocaleTag);
        if (bundle != null) {
            msg = bundle.getRaw(key);
            if (msg != null)
                return msg;
        }
        return key;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The {@link String} of the localization by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     */
    public String getRaw(@NotNull final String key, @NotNull final Locale localeTag) {
        if (isClientOriented)
            return getClientOriented(key, localeTag);
        else return getRaw(key);
    }

    /**
     * @param key The key of the localized message.
     * @return The deserialized {@link MiniMessage} {@link Component} of the localization default locale.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     */
    public Component get(@NotNull final String key) {
        return MiniMessage.miniMessage().deserialize(getRaw(key));
    }

    /**
     * @param key The key of the localized message.
     * @param placeholders The message placeholders.
     * @return The {@link MiniMessage} {@link Component} of the localization default locale with placeholders.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     */
    public Component get(@NotNull final String key, String... placeholders) {
        String text = getRaw(key);
        try {
            text = MessageFormat.format(text, (Object) placeholders);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid placeholders under the key \"" + key + "\" of default localization (\"" + this.defaultLocaleTag + "\")!");
        } catch (NullPointerException ex) {
            plugin.getLogger().warning("Placeholders under the key \"" + key + "\" of default localization (\"" + this.defaultLocaleTag + "\") are null!");
        }
        return MiniMessage.miniMessage().deserialize(text);
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The deserialized {@link MiniMessage} {@link Component} of the localization by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     */
    public Component get(@NotNull final String key, @NotNull final Locale localeTag) {
        return MiniMessage.miniMessage().deserialize(getRaw(key, localeTag));
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @param placeholders The message placeholders.
     * @return The deserialized {@link MiniMessage} {@link Component} of the localization by specified locale tag with placeholders.
     */
    public Component get(@NotNull final String key, @NotNull final Locale localeTag, @NotNull String... placeholders) {
        String text = getRaw(key, localeTag);
        try {
            text = MessageFormat.format(text, (Object) placeholders);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization!");
        } catch (NullPointerException ex) {
            plugin.getLogger().warning("Placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization are null!");
        }
        return MiniMessage.miniMessage().deserialize(text);
    }

    /**
     * @param key The key of the localized message.
     * @return The raw localized {@link String} list of numbered messages of default plugin localization.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization element, if
     * it's null this method will return reference default localization element
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<String> getNumberedListRaw(@NotNull final String key) {
        List<String> text = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n);
                text.add(element);
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return text;
    }

    /**
     * @param key The key of the localized message.
     * @return The deserialized {@link MiniMessage} {@link Component} list of numbered messages of default plugin localization.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedList(@NotNull final String key) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                Component element = get(key + "." + n);
                components.add(element);
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @return The deserialized lore normalized (white and disabled italic) {@link MiniMessage} {@link Component} list of numbered messages of default plugin localization.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedListLoreNormalized(@NotNull final String key) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n);
                components.add(MiniMessage.miniMessage().deserialize("<white><i:false>" + element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @return The deserialized {@link MiniMessage} {@link Component} list of numbered messages of default plugin localization with placeholders.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedList(@NotNull final String key, String... placeholders) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String actualKey = key + "." + n;
                String element = getRaw(actualKey);
                try {
                    element = MessageFormat.format(element, (Object) placeholders);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Invalid placeholders under the key \"" + actualKey + "\" of default localization (\"" + this.defaultLocaleTag + "\")!");
                } catch (NullPointerException ex) {
                    plugin.getLogger().warning("Placeholders under the key \"" + actualKey + "\" of default localization (\"" + this.defaultLocaleTag + "\") are null!");
                }
                components.add(MiniMessage.miniMessage().deserialize(element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @return The deserialized lore normalized (white and disabled italic) {@link MiniMessage} {@link Component} list of numbered messages of default plugin localization with placeholders.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override default localization string, if
     * it's null this method will return reference default localization string
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedListLoreNormalized(@NotNull final String key, @NotNull final String... placeholders) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String actualKey = key + "." + n;
                String element = getRaw(actualKey);
                try {
                    element = MessageFormat.format(element, (Object) placeholders);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Invalid placeholders under the key \"" + actualKey + "\" of default localization (\"" + this.defaultLocaleTag + "\")!");
                } catch (NullPointerException ex) {
                    plugin.getLogger().warning("Placeholders under the key \"" + actualKey + "\" of default localization (\"" + this.defaultLocaleTag + "\") are null!");
                }
                components.add(MiniMessage.miniMessage().deserialize("<white><i:false>" + element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The deserialized {@link MiniMessage} {@link String} list of numbered messages by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<String> getNumberedListRaw(@NotNull final String key, @NotNull final Locale localeTag) {
        List<String> text = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n, localeTag);
                text.add(element);
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return text;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The {@link Component} list of numbered messages by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedList(@NotNull final String key, @NotNull final Locale localeTag) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                Component element = get(key + "." + n, localeTag);
                n++;
                components.add(element);
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The deserialized lore normalized (white and disabled italic) {@link Component} list of numbered messages by specified locale tag.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedListLoreNormalized(@NotNull final String key, @NotNull final Locale localeTag) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n, localeTag);
                components.add(MiniMessage.miniMessage().deserialize("<white><i:false>" + element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The deserialized {@link Component} list of numbered messages of plugin localization by specified locale tag with placeholders.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedList(@NotNull final String key, @NotNull final Locale localeTag, @NotNull final String... placeholders) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n, localeTag);
                try {
                    element = MessageFormat.format(element, (Object) placeholders);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Invalid placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization!");
                } catch (NullPointerException ex) {
                    plugin.getLogger().warning("Placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization are null!");
                }
                components.add(MiniMessage.miniMessage().deserialize(element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }

    /**
     * @param key The key of the localized message.
     * @param localeTag The string of the localization by specified locale tag.
     * @return The deserialized lore normalized (white and disabled italic) {@link Component} list of numbered messages by specified locale tag with placeholders.
     * <p></p>
     * <b>NOTE:</b> Firstly method will return override localization string by locale tag, if
     * it's null this method will return reference localization string by locale tag, if
     * it's null this method will return default reference localization specified in setup of {@link Localization}
     * and if it's null too then this method will return the specified key.
     * <p></p>
     * <b>EXAMPLE</b> OF NUMBERED LIST IN LOCALIZATION BUNDLE FILE:
     * <p>locale.tag=en_US</p>
     * <p>...</p>
     * <p>message.example.1=This is a text which</p>
     * <p>message.example.2=will return as list string</p>
     * <p>message.example.3=(if raw)/component. Key</p>
     * <p>message.example.4=of this example text is "message.example"!</p>
     */
    public List<Component> getNumberedListLoreNormalized(@NotNull final String key, @NotNull final Locale localeTag, @NotNull final String... placeholders) {
        List<Component> components = new ArrayList<>();
        int n = 1;
        while (true) {
            try {
                String element = getRaw(key + "." + n, localeTag);
                try {
                    element = MessageFormat.format(element, (Object) placeholders);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Invalid placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization!");
                } catch (NullPointerException ex) {
                    plugin.getLogger().warning("Placeholders under the key \"" + key + "\" of \"" + localeTag + "\" localization are null!");
                }
                components.add(MiniMessage.miniMessage().deserialize("<white><i:false>" + element));
                n++;
            } catch (MissingResourceException e) {
                break;
            }
        }
        return components;
    }
}