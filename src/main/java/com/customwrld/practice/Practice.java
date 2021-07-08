package com.customwrld.practice;

import com.customwrld.customlib.CustomLib;
import com.customwrld.customlib.util.ClassUtils;
import com.customwrld.practice.arena.Arena;
import com.customwrld.practice.command.*;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.event.Event;
import com.customwrld.practice.events.BlockEvents;
import com.customwrld.practice.events.EntityEvents;
import com.customwrld.practice.events.InventoryEvents;
import com.customwrld.practice.events.PlayerEvents;
import com.customwrld.practice.kit.Kit;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.Command;
import com.customwrld.practice.util.WorldUtil;
import com.mongodb.client.MongoDatabase;
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

@Getter
@Setter
public class Practice extends JavaPlugin {

    @Getter private static Practice practice;

    private ManagerHandler managerHandler;
    private MongoDatabase database;
    private Location spawn;
    private WorldEditPlugin worldEditPlugin;
    private Event activeEvent;
    private List<Command> registeredCommands;

    @Override
    public void onEnable() {
        practice = this;
        this.database = CustomLib.getCustomLib().getMongoDatabase();
        this.managerHandler = new ManagerHandler();
        this.spawn = new Location(WorldUtil.WORLD, 0, 64, 0);
        this.worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        this.activeEvent = null;
        this.registeredCommands = new ArrayList<>();

        Arrays.asList(
                new PlayerEvents(),
                new BlockEvents(),
                new InventoryEvents(),
                new EntityEvents()
        ).forEach(events -> Bukkit.getPluginManager().registerEvents(events, this));

        CommandAPI.unregister("spectate");

        for (Class<?> aClass : ClassUtils.getClassesInPackage(Practice.class, "com.customwrld.practice.command")) {
            registerCommand(this, aClass);
        }

        this.managerHandler.getArenaManager().loadAll().forEach((arena -> {
            if (!this.managerHandler.getArenaManager().getArenas().containsKey(arena.getName())) {
                this.managerHandler.getArenaManager().getArenas().put(arena.getName(), new ArrayList<>());
            } else {
                this.managerHandler.getArenaManager().getArenas().get(arena.getName()).add(arena);
            }
        }));

        this.managerHandler.getKitManager().loadAll().forEach((kit -> this.managerHandler.getKitManager().getKits().put(kit.getName(), kit)));
    }

    @Override
    public void onDisable() {
        for (Duel duel : this.managerHandler.getDuelManager().getActiveDuels().values()) {
            duel.end(Duel.Result.DRAW, true);
        }

        for (PracticePlayer practicePlayer : this.managerHandler.getPracticePlayerManager().getPlayers()) {
            this.managerHandler.getPracticePlayerManager().save(practicePlayer);
        }

        for (List<Arena> arenaList : this.managerHandler.getArenaManager().getArenas().values()) {
            for (Arena arena : arenaList) {
                this.managerHandler.getArenaManager().save(arena);
            }
        }

        for (Kit kit : this.managerHandler.getKitManager().getKits().values()) {
            this.managerHandler.getKitManager().save(kit);
        }
    }

    private static boolean registerCommand(Practice practice, Class<?> commandClass) {
        try {
            if (!Command.class.isAssignableFrom(commandClass)) {
                return false;
            }
            Command command = (Command) commandClass.getDeclaredConstructor().newInstance();
            command.command().register();
            practice.getRegisteredCommands().add(command);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

}
