package me.theorenter.configurablevillagers.object;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VillagerWrapper {
    private final Villager villager;
    private final NamespacedKey tradesNSK = new NamespacedKey("configurablevillagers", "trades");

    public VillagerWrapper(@NotNull final Villager villager) {
        this.villager = villager;

        PersistentDataContainer pdc = villager.getPersistentDataContainer();

        if (!pdc.has(tradesNSK)) {
            pdc.set(
                    tradesNSK,
                    PersistentDataType.LIST.strings(),
                    new ArrayList<>()
            );
        }
    }

    public List<String> getCustomTradesIdentifiers() {
        return this.villager.getPersistentDataContainer().get(this.tradesNSK,  PersistentDataType.LIST.strings());
    }

    @SuppressWarnings("all")
    public void addTradeIdentifier(@NotNull final String ID) {
        List IDs = new ArrayList(this.villager.getPersistentDataContainer().get(this.tradesNSK, PersistentDataType.LIST.strings()));
        IDs.add(ID);
        this.villager.getPersistentDataContainer().set(this.tradesNSK, PersistentDataType.LIST.strings(), IDs);
    }

    public boolean hasCustomTrade(@NotNull final String ID) {
        return this.getCustomTradesIdentifiers().contains(ID);
    }

    @SuppressWarnings("unused")
    public boolean hasCustomTrade(@NotNull final CustomTrade trade) {
        return this.getCustomTradesIdentifiers().contains(trade.getID());
    }

    public void clearTradeData() {
        this.villager.getPersistentDataContainer().remove(this.tradesNSK);
    }

}
