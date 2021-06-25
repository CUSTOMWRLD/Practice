package com.customwrld.custompractice;

import com.customwrld.customlib.CustomLib;
import com.customwrld.custompractice.arena.Arena;
import com.customwrld.custompractice.command.*;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.event.Event;
import com.customwrld.custompractice.events.BlockEvents;
import com.customwrld.custompractice.events.InventoryEvents;
import com.customwrld.custompractice.events.PlayerEvents;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.ManagerHandler;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.util.Placeholders;
import com.customwrld.custompractice.util.WorldUtil;
import com.mongodb.client.MongoDatabase;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CustomPractice extends JavaPlugin {

    @Getter private static CustomPractice customPractice;

    @Getter private static ManagerHandler managerHandler;

    @Getter private static MongoDatabase database;

    @Getter private static Location spawn;

    @Getter private static WorldEditPlugin worldEditPlugin;

    @Getter private Placeholders placeholders;

    @Getter @Setter private Event activeEvent;

    @Override
    public void onEnable() {
        customPractice = this;
        database = CustomLib.getCustomLib().getMongoDatabase();
        managerHandler = new ManagerHandler();
        spawn = new Location(WorldUtil.WORLD, 0, 64, 0);
        worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        activeEvent = null;

        this.placeholders = new Placeholders("CUSTOMWRLD", "practice", this.getDescription().getVersion());
        this.placeholders.register();

        Arrays.asList(
                new PlayerEvents(),
                new BlockEvents(),
                new InventoryEvents()
        ).forEach(events -> {
            Bukkit.getPluginManager().registerEvents(events, this);
        });

        CommandAPI.unregister("spectate");

        Arrays.asList(
                DuelCommand.command(),
                AcceptCommand.command(),
                ArenaCommand.command(),
                KitCommand.command(),
                SpectateCommand.command()
        ).forEach(CommandAPICommand::register);

        managerHandler.getArenaManager().loadAll().forEach((arena -> {
            if (!managerHandler.getArenaManager().getArenas().containsKey(arena.getName())) {
                managerHandler.getArenaManager().getArenas().put(arena.getName(), new ArrayList<>());
            } else {
                managerHandler.getArenaManager().getArenas().get(arena.getName()).add(arena);
            }
        }));
        managerHandler.getKitManager().loadAll().forEach((kit -> managerHandler.getKitManager().getKits().put(kit.getName(), kit)));
    }



    @Override
    public void onDisable() {
        for (Duel duel : managerHandler.getDuelManager().getActiveDuels().values()) {
            duel.end(Duel.Result.DRAW, true);
        }
        for (PracticePlayer practicePlayer : managerHandler.getPracticePlayerManager().getPlayers()) {
            managerHandler.getPracticePlayerManager().save(practicePlayer);
        }
        for (List<Arena> arenaList : managerHandler.getArenaManager().getArenas().values()) {
            for (Arena arena : arenaList) {
                managerHandler.getArenaManager().save(arena);
            }
        }
        for (Kit kit : managerHandler.getKitManager().getKits().values()) {
            managerHandler.getKitManager().save(kit);
        }
    }
}
