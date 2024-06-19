package me.theorenter.configurablevillagers.listeners;

import me.theorenter.configurablevillagers.object.VillagerWrapper;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.jetbrains.annotations.NotNull;

public final class VillagerChangeProfessionListener implements Listener {

    public VillagerChangeProfessionListener() {
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVillagerChangeProfession(@NotNull final VillagerCareerChangeEvent e) {
        Villager.Profession profession = e.getProfession();
        if (profession == Villager.Profession.NITWIT || profession == Villager.Profession.NONE)
            new VillagerWrapper(e.getEntity()).clearTradeData();

    }
}
