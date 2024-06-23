package me.theorenter.configurablevillagers;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import me.theorenter.configurablevillagers.commands.ConfigurableVillagersCMD;
import me.theorenter.configurablevillagers.listeners.EntityTransformListener;
import me.theorenter.configurablevillagers.listeners.VillagerAcquireTradeEventListener;
import me.theorenter.configurablevillagers.listeners.VillagerChangeProfessionListener;
import me.theorenter.configurablevillagers.localization.Localization;
import me.theorenter.configurablevillagers.utils.TradeLoader;
import me.theorenter.configurablevillagers.utils.TradeStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main class.
 */
public final class ConfigurableVillagers extends JavaPlugin {

    // Config
    private Config config;

    // Localization
    private Localization localization;

    // Loaders
    private TradeLoader tradeLoader;

    // Storages
    private TradeStorage tradeStorage;

    // Listeners
    private final VillagerAcquireTradeEventListener villagerAcquireTradeEventListener = new VillagerAcquireTradeEventListener(this);
    private final VillagerChangeProfessionListener villagerChangeProfessionListener = new VillagerChangeProfessionListener();
    private final EntityTransformListener entityTransformListener = new EntityTransformListener(this);
    private Logger log;

    /**
     * Plugin load logic.
     */
    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    /**
     * Plugin enable logic.
     */
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onEnable() {
        Bukkit.getLogger().info("================     ConfigurableVillagers     =================");
        this.log = getLogger();
        loadConfig();
        loadLocalization();
        if (config.USE_CUSTOM_TRADES) loadLoaders();
        loadStorages();

        // Load all trades
        if (config.USE_CUSTOM_TRADES) tradeLoader.loadAll();


        // Register listeners
        registerListeners();

        // Commands
        registerCommands();
        showStatus();
    }

    /**
     * Plugin disable logic.
     */
    @Override
    public void onDisable() {
        if (config.USE_CUSTOM_TRADES) unregisterListeners();
        showStatus();
    }

    @SuppressWarnings("UnstableApiUsage")
    public void onReload() {
        Bukkit.getLogger().info("============     ConfigurableVillagers (reload)     ============");
        this.log = getLogger();
        loadConfig();
        loadLocalization();
        if (config.USE_CUSTOM_TRADES) loadLoaders();
        loadStorages();

        // Load all trades
        if (config.USE_CUSTOM_TRADES) tradeLoader.loadAll();

        // Register listeners
        registerListeners();

        // Commands
        registerCommands();
        showStatus();
    }

    /**
     * Shows the plugin enabling status.
     */
    @SuppressWarnings("UnstableApiUsage")
    private void showStatus() {
        Bukkit.getLogger().info("================================================================");
        if(this.isEnabled())
            log.info("Version: "+ getPluginMeta().getVersion() + " – Plugin Enabled");
        else
            log.info("Version: "+ getPluginMeta().getVersion() + " – Plugin Disabled");
        Bukkit.getLogger().info("================================================================");
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadConfig() {
        try {
            this.config = new Config(this);
            log.info("Configuration successfully loaded.");
        } catch (IOException | InvalidConfigurationException ex) {
            this.setEnabled(false);
            log.severe("An error occurred while loading the configuration:");
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
    private void loadLocalization() {
        try {
            this.localization = new Localization(this,
                    config.LOCALIZATION_CLIENT_ORIENTED,
                    "settings" + File.separator + "localization",
                    "reference",
                    config.DEFAULT_LOCALIZATION);
            log.info("Localization successfully loaded.");
        } catch (IOException | InvalidConfigurationException ex) {
            this.setEnabled(false);
            log.severe("An error occurred while loading the localization:");
            throw new RuntimeException(ex);
        }
    }

    private void loadStorages() {
        if (config.USE_CUSTOM_TRADES) {
            this.tradeStorage = new TradeStorage();
        } else this.tradeStorage = null;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadLoaders() {
        try {
            if (config.USE_CUSTOM_TRADES) {
                this.tradeLoader = new TradeLoader(this);
            } else {
                log.info("Custom trade offers were not loaded because they are disabled in configurations (\"settings.use-custom-trades\" value in configuration.yml)");
            }
            log.info("Loaders successfully loaded.");
        } catch (IOException | InvalidConfigurationException ex) {
            this.setEnabled(false);
            log.severe("An error occurred while loading the loaders:");
            throw new RuntimeException(ex);
        }
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(villagerAcquireTradeEventListener, this);
        pm.registerEvents(villagerChangeProfessionListener, this);
        pm.registerEvents(entityTransformListener, this);
        log.info("Listeners successfully registered.");
    }

    private void registerCommands() {
        CommandAPI.onEnable();
        new ConfigurableVillagersCMD(this).register();

        log.info("Plugin commands successfully loaded.");
    }

    private void unregisterListeners() {
        HandlerList.unregisterAll(villagerAcquireTradeEventListener);
        HandlerList.unregisterAll(villagerChangeProfessionListener);
        HandlerList.unregisterAll(entityTransformListener);
        log.info("Listeners successfully unregistered.");
    }

    @NotNull
    public Config getCfg() {
        return this.config;
    }

    @NotNull
    public Localization getLoc() {
        return localization;
    }

    @NotNull
    public TradeStorage getTradeStorage() {
        return tradeStorage;
    }
}
