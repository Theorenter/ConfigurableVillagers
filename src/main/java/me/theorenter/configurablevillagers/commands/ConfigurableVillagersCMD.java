package me.theorenter.configurablevillagers.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.UUIDArgument;
import me.theorenter.configurablevillagers.ConfigurableVillagers;
import me.theorenter.configurablevillagers.object.CustomTrade;
import me.theorenter.configurablevillagers.object.VillagerWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ConfigurableVillagersCMD {
    private final ConfigurableVillagers plugin;
    public ConfigurableVillagersCMD(@NotNull final ConfigurableVillagers plugin) { this.plugin = plugin; }
    public void register() {
        List<Argument<?>> entIdentifierDynamicArg = new ArrayList<>();
        entIdentifierDynamicArg.add(new UUIDArgument("UUID").includeSuggestions(ArgumentSuggestions.strings(
                info -> {
                    if (info.sender() instanceof Player) {
                        Player p = (Player) info.sender();
                        Entity e = p.getTargetEntity(16);

                        if (e == null) {
                            return new String[]{""};
                        }

                        if (!(e instanceof LivingEntity)) {
                            return new String[]{""};
                        }

                        LivingEntity le = (LivingEntity) e;
                        return new String[]{le.getUniqueId().toString()};
                    } else {
                        List<UUID> uuids = new ArrayList<>();
                        plugin.getServer().getWorlds().forEach(world -> {
                            world.getLivingEntities().forEach(livingEntity -> {
                                if (livingEntity instanceof Villager) {
                                    Villager v = (Villager) livingEntity;
                                    if (v.getProfession() != Villager.Profession.NITWIT && v.getProfession() != Villager.Profession.NONE)
                                        uuids.add(v.getUniqueId());
                                }
                            });
                        });
                        return new String[]{uuids.toString()};
                    }
                }
        )));

        CommandAPICommand cvReroll = new CommandAPICommand("reroll")
                .withPermission("configurablevillagers.command.configurablevillagers.reroll")
                .withArguments(entIdentifierDynamicArg)
                .executes((executor, args) -> {
                    if (executor instanceof Player) {
                        Player p = (Player) executor;

                        if (args.count() < 1) {
                            Component msg = plugin.getLoc().get("message.error.invalid_arguments_count", p.locale());
                            p.sendMessage(msg);
                            return;
                        }
                        Entity e = plugin.getServer().getEntity((UUID) args.get(0));

                        if (e == null) {
                            Component msg = plugin.getLoc().get("message.error.entity_with_specified_id_doesn't_exists", p.locale());
                            p.sendMessage(msg);
                            return;
                        }

                        if (!(e instanceof Villager)) {
                            Component msg = plugin.getLoc().get("message.error.entity_not_a_villager", p.locale());
                            p.sendMessage(msg);
                            return;
                        }

                        Villager v = (Villager) e;
                        VillagerWrapper vw = new VillagerWrapper(v);
                        vw.clearTradeData();
                        List<MerchantRecipe> rerolledRecipes = new ArrayList<>();

                        if (v.getRecipeCount() == 0) {
                            Component msg = plugin.getLoc().get("message.error.no_trades", p.locale());
                            p.sendMessage(msg);
                            return;
                        }

                        for (int i = 0; i < v.getVillagerLevel()*2; i++) {
                            int pseudoLevel = (int) Math.ceil((1.0 + i) / 2);
                            CustomTrade ct = plugin.getTradeStorage().getRandomAvailableTrade(v, pseudoLevel);
                            if (ct != null) {
                                rerolledRecipes.add(ct.getRecipe());
                                vw.addTradeIdentifier(ct.getID());
                            }
                        }
                        v.setRecipes(rerolledRecipes);

                        Component msg = plugin.getLoc().get("message.notification.trades_successfully_rerolled", p.locale());
                        p.sendMessage(msg);
                    } else {
                        executor.sendMessage("This command only for players!");
                    }
                });

        CommandAPICommand cvReload = new CommandAPICommand("reload")
                .withPermission("configurablevillagers.command.configurablevillagers.reload")
                .executesPlayer(executor -> {
                    Player p = executor.sender();
                    plugin.getLogger().info(p.getName() + " (" + (p).getUniqueId() + ") reloads the plugin!");
                    plugin.onReload();

                    Component msg;
                    if (plugin.isEnabled()) {
                        msg = plugin.getLoc().get("message.notification.plugin_successfully_reload", p.locale());
                    } else {
                        msg = plugin.getLoc().get("message.error.invalid_reload", p.locale());
                    }
                    p.sendMessage(msg);
                })
                .executesConsole(sender -> {
                    sender.sender().sendMessage("Start plugin reloading...");
                    plugin.onReload();
                });

        // Main commands.
        new CommandAPICommand("configurablevillagers")
                .withAliases("cv")
                .withPermission("configurablevillagers.command.configurablevillagers.help")
                .withSubcommand(cvReload)
                .withSubcommand(cvReroll)
                .executesPlayer(playerExecutor -> {
                    Player p = playerExecutor.sender();
                    List<Component> board = plugin.getLoc().getNumberedList("message.board.cv.help", p.locale());
                    board.forEach(p::sendMessage);
                })
                .executesConsole(consoleExecutor -> {
                    ConsoleCommandSender console = consoleExecutor.sender();

                    console.sendMessage("ConfigurableVillagers – Help");
                    console.sendMessage("/cv reload – Reloads the plugin;");
                    console.sendMessage("/cv reroll <villager_UUID> – Rerolls villager trades;");
                })
                .register(plugin);
    }
}
