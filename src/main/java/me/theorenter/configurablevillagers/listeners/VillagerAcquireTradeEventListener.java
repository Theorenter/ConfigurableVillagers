package me.theorenter.configurablevillagers.listeners;

import me.theorenter.configurablevillagers.ConfigurableVillagers;
import me.theorenter.configurablevillagers.object.CustomTrade;
import me.theorenter.configurablevillagers.object.VillagerWrapper;
import me.theorenter.configurablevillagers.utils.TradeStorage;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.jetbrains.annotations.NotNull;

public final class VillagerAcquireTradeEventListener implements Listener {

    private final ConfigurableVillagers plugin;

    public VillagerAcquireTradeEventListener(@NotNull final ConfigurableVillagers plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVillagerAcquireTrade(@NotNull final VillagerAcquireTradeEvent e) {

        if (e.getEntity() instanceof WanderingTrader)
            return;

        if (e.getRecipe().getResult().getType() == Material.FILLED_MAP && plugin.getCfg().USE_VANILLA_CARTOGRAPHER_MAPS)
            return;

        Villager villager = (Villager) e.getEntity();

        if (villager instanceof WanderingTrader)
            return;

        VillagerWrapper vw = new VillagerWrapper(villager);

        TradeStorage tradeStorage = plugin.getTradeStorage();
        CustomTrade trade = tradeStorage.getRandomAvailableTrade(villager);

        if (trade != null) {
            e.setRecipe(trade.getRecipe());
            vw.addTradeIdentifier(trade.getID());
        } else {
            e.setCancelled(true);
        }
    }
}
