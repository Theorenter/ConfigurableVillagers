package me.theorenter.configurablevillagers.listeners;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import me.theorenter.configurablevillagers.ConfigurableVillagers;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class EntityTransformListener implements Listener {

    private final ConfigurableVillagers plugin;
    public EntityTransformListener(@NotNull final ConfigurableVillagers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("ConstantConditions")
    public void onVillagerTransform(@NotNull final EntityTransformEvent e) {
        if (e.getEntity() instanceof Villager) {
            Villager villager = (Villager) e.getEntity();

            NamespacedKey NSK = new NamespacedKey("configurablevillagers", "trades");
            List<String> tradeIdentifiers = villager.getPersistentDataContainer().get(NSK, PersistentDataType.LIST.strings());
            if (tradeIdentifiers == null) {
                return;
            }
            e.getTransformedEntity().getPersistentDataContainer().set(NSK, PersistentDataType.LIST.strings(), tradeIdentifiers);
            return;
        }

        if (e.getEntity() instanceof ZombieVillager) {
            ZombieVillager z = (ZombieVillager) e.getEntity();

            NamespacedKey NSK = new NamespacedKey("configurablevillagers", "trades");
            List<String> tradeIdentifiers = z.getPersistentDataContainer().get(NSK, PersistentDataType.LIST.strings());
            if (tradeIdentifiers == null) {
                return;
            }
            PersistentDataContainer PDC = e.getTransformedEntity().getPersistentDataContainer();
            PDC.set(NSK, PersistentDataType.LIST.strings(), tradeIdentifiers);

            Villager villager = (Villager) e.getTransformedEntity();

            if (z.getConversionPlayer() != null) {
                UUID healerUUID = z.getConversionPlayer().getUniqueId();
                System.out.println(healerUUID + " – хиллер!");

                Reputation rep = villager.getReputation(healerUUID);
                if (plugin.getCfg().MAJOR_POSITIVE_FIXED)
                    rep.setReputation(ReputationType.MAJOR_POSITIVE, plugin.getCfg().MAJOR_POSITIVE_VALUE);

                if (plugin.getCfg().MINOR_POSITIVE_FIXED)
                    rep.setReputation(ReputationType.MINOR_POSITIVE, plugin.getCfg().MINOR_POSITIVE_VALUE);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        villager.setReputation(healerUUID, rep);
                    }
                }.runTaskLater(plugin, 1);
            }
        }
    }

    /*@EventHandler
    public void onVillagerClick(@NotNull final PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Villager && e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.CLOCK)) {
            Villager v = (Villager) e.getRightClicked();
            Reputation rep = v.getReputation(e.getPlayer().getUniqueId());
            if (rep == null)
                return;
            System.out.println("                 TRADING: " + rep.getReputation(ReputationType.TRADING));
            System.out.println("MAJOR_NEGATIVE (killing): " + rep.getReputation(ReputationType.MAJOR_NEGATIVE));
            System.out.println(" MINOR_NEGATIVE (attack): " + rep.getReputation(ReputationType.MINOR_NEGATIVE));
            System.out.println(" MINOR_POSITIVE (curing): " + rep.getReputation(ReputationType.MINOR_POSITIVE));
            System.out.println("MAJOR_POSITIVE: (curing): " + rep.getReputation(ReputationType.MAJOR_POSITIVE));
            return;
        }

        if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.BRUSH)) {
            ZombieVillager zv = (ZombieVillager) e.getRightClicked();
            zv.setConversionPlayer(e.getPlayer());
            zv.setConversionTime(10);
        }
    }*/
}
