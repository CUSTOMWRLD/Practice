package com.customwrld.practice.command;

import com.customwrld.customlib.util.Cuboid;
import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.arena.Arena;
import com.customwrld.practice.managers.ArenaManager;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.util.Command;
import com.customwrld.practice.util.WorldUtil;
import com.mongodb.client.model.Filters;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArenaCommand extends Command {

    private final ManagerHandler handler;

    public ArenaCommand() {
        this.handler = Practice.getPractice().getManagerHandler();
        this.command = "arena";
        this.permission = "practice.admin";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withPermission(this.permission)
                .withSubcommand(this.create())
                .withSubcommand(this.list())
                .withSubcommand(this.setlocation())
                .withSubcommand(this.enable())
                .withSubcommand(this.disable())
                .withSubcommand(this.setRegion())
                .withSubcommand(this.listLocked())
                .withSubcommand(this.lock())
                .withSubcommand(this.unlock())
                .withSubcommand(this.delete())
                .withSubcommand(this.deleteall())
                .withSubcommand(this.stack());
    }

    private CommandAPICommand create() {
        return new CommandAPICommand("create")
                .withArguments(new StringArgument("name"))
                .executesPlayer((sender, args) -> {
                    Arena arena = new Arena((String) args[0], sender.getLocation(), sender.getLocation());
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    if (!arenaManager.getArenas().containsKey(arena.getName())) {
                        arenaManager.getArenas().put(arena.getName(), new ArrayList<>());
                        arenaManager.getArenas().get(arena.getName()).add(arena);
                    } else {
                        arenaManager.getArenas().get(arena.getName()).add(arena);
                        arena.setLockedKits(new ArrayList<>(arenaManager.getArenas().get(arena.getName()).get(0).getLockedKits()));
                    }
                    arenaManager.save(arena);
                    Component message = Component.text("Arena successfully created, click here to copy the uuid.").clickEvent(ClickEvent.copyToClipboard(arena.getUuid().toString()));
                    sender.sendMessage(message);
                });
    }

    private CommandAPICommand list() {
        return new CommandAPICommand("list")
                .withArguments(new StringArgument("name"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    if (!arenaManager.getArenas().containsKey((String) args[0])) {
                        for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                            sender.sendMessage(entry.getKey() + " | " + entry.getValue().size());
                        }
                        return;
                    }
                    for (Arena arena : arenaManager.getArenas().get((String) args[0])) {
                        Component message = Component.text(arena.getUuid().toString()).clickEvent(ClickEvent.copyToClipboard(arena.getUuid().toString()));
                        Component message1 = Component.text("Click to teleport");
                        if (arena.getTeamALocation() != null) {
                            message1 = message1.hoverEvent(HoverEvent.showText(Component.text("Teleport to Team A spawn", MC.CC.DARK_BLUE.getTextColor())));
                            message1 = message1.clickEvent(ClickEvent.runCommand("/tp " + arena.getTeamALocation().getBlockX() + " " + arena.getTeamALocation().getBlockY() + " " + arena.getTeamALocation().getBlockZ()));
                        } else if (arena.getTeamBLocation() != null) {
                            message1 = message1.hoverEvent(HoverEvent.showText(Component.text("Teleport to Team B spawn", MC.CC.DARK_RED.getTextColor())));
                            message1 = message1.clickEvent(ClickEvent.runCommand("/tp " + arena.getTeamBLocation().getBlockX() + " " + arena.getTeamBLocation().getBlockY() + " " + arena.getTeamBLocation().getBlockZ()));
                        }
                        sender.sendMessage(message);
                        sender.sendMessage(message1);
                    }
                });
    }

    private CommandAPICommand setlocation() {
        return new CommandAPICommand("setlocation")
                .withArguments(new UUIDArgument("uuid"))
                .withArguments(new StringArgument("A/B"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;
                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (Arena arena1 : arenaList) {
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                            }
                        }
                    }
                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid", MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (((String) args[1]).equalsIgnoreCase("a")) {
                        arena.setTeamALocation(sender.getLocation());
                    } else if (((String) args[1]).equalsIgnoreCase("b")) {
                        arena.setTeamBLocation(sender.getLocation());
                    } else {
                        sender.sendMessage(Component.text("Please provide either A or B", MC.CC.RED.getTextColor()));
                        return;
                    }
                    sender.sendMessage(Component.text("Set location for team " + args[1] + "!", MC.CC.GREEN.getTextColor()));
                    arenaManager.save(arena);
                });
    }

    private CommandAPICommand enable() {
        return new CommandAPICommand("enable")
                .withArguments(new UUIDArgument("uuid"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;
                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (Arena arena1 : arenaList) {
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                            }
                        }
                    }

                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid", MC.CC.RED.getTextColor()));
                        return;
                    }

                    arena.setEnabled(true);
                    arenaManager.save(arena);
                });
    }

    private CommandAPICommand disable() {
        return new CommandAPICommand("disable")
                .withArguments(new UUIDArgument("uuid"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;

                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (Arena arena1 : arenaList) {
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                            }
                        }
                    }

                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid", MC.CC.RED.getTextColor()));
                        return;
                    }

                    arena.setEnabled(false);
                    arenaManager.save(arena);
                });
    }

    private CommandAPICommand setRegion() {
        return new CommandAPICommand("setRegion")
                .withArguments(new UUIDArgument("uuid"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;

                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (Arena arena1 : arenaList) {
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                            }
                        }
                    }

                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    Region region;

                    try {
                        region = Practice.getPractice().getWorldEditPlugin().getSession(sender).getSelection();
                    } catch (IncompleteRegionException e) {
                        sender.sendMessage(Component.text("Please make a WorldEdit selection first!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    if (region == null) {
                        sender.sendMessage(Component.text("There was an error trying to get the region!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    arena.setCuboid(new Cuboid(new Location(WorldUtil.WORLD, region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ()),
                            new Location(WorldUtil.WORLD, region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ())));
                    WorldUtil.saveSchematic(WorldUtil.WORLD, Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder(),"schematics/" + arena.getName() +".schem", arena.getCuboid());
                    arenaManager.save(arena);
                    sender.sendMessage(Component.text("Region has beeen successfully updated!", MC.CC.GREEN.getTextColor()));
                });
    }

    private CommandAPICommand listLocked() {
        return new CommandAPICommand("listlocked")
                .executes((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                        sender.sendMessage(Component.text(entry.getKey() + ": " + entry.getValue().get(0).getLockedKits()));
                    }
                });
    }

    private CommandAPICommand lock() {
        return new CommandAPICommand("lock")
                .withArguments(new StringArgument("arena"))
                .withArguments(new StringArgument("kit"))
                .executes((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase((String) args[0])) {
                            for (Arena arena : entry.getValue()) {
                                if (!arena.getLockedKits().contains((String) args[1])) {
                                    arena.getLockedKits().add((String) args[1]);
                                    arenaManager.save(arena);
                                    sender.sendMessage(Component.text("Arena " + arena.getUuid().toString() + " has successfully been locked to this kit.", MC.CC.GREEN.getTextColor()));
                                } else {
                                    sender.sendMessage(Component.text("This arena is already locked to this kit.", MC.CC.RED.getTextColor()));
                                }
                            }
                        }
                    }
                    //sender.sendMessage(Component.text("Please enter a valid kit!", MC.CC.RED.getTextColor()));
                });
    }

    private CommandAPICommand unlock() {
        return new CommandAPICommand("lock")
                .withArguments(new StringArgument("arena"))
                .withArguments(new StringArgument("kit"))
                .executes((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase((String) args[0])) {
                            for (Arena arena : entry.getValue()) {
                                if (arena.getLockedKits().contains((String) args[1])) {
                                    arena.getLockedKits().remove((String) args[1]);
                                    arenaManager.save(arena);
                                    sender.sendMessage(Component.text("Arena has successfully been unlocked from arena " + arena.getUuid().toString() + ".", MC.CC.GREEN.getTextColor()));
                                } else {
                                    sender.sendMessage(Component.text("This arena isn't locked to this kit.", MC.CC.RED.getTextColor()));
                                }
                            }
                        }
                    }
                    sender.sendMessage(Component.text("Please enter a valid kit!", MC.CC.RED.getTextColor()));
                });
    }

    private CommandAPICommand delete() {
        return new CommandAPICommand("delete")
                .withArguments(new UUIDArgument("uuid"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;
                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (final var it = arenaList.iterator(); it.hasNext();) {
                            var arena1 = it.next();
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                                it.remove();
                            }
                        }
                    }
                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid", MC.CC.RED.getTextColor()));
                        return;
                    }
                    arenaManager.getArenaCollection().deleteOne(Filters.eq(arena.getId()));
                });
    }

    private CommandAPICommand deleteall() {
        return new CommandAPICommand("deleteall")
                .withArguments(new StringArgument("name"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    if (arenaManager.getArenas().containsKey((String) args[0])) {
                        for (Arena arena : arenaManager.getArenas().get((String) args[0])) {
                            arenaManager.getArenaCollection().deleteOne(Filters.eq(arena.getId()));
                        }
                        arenaManager.getArenas().remove((String) args[0]);
                    }
                });
    }

    private CommandAPICommand stack() {
        return new CommandAPICommand("stack")
                .withArguments(new UUIDArgument("uuid"))
                .withArguments(new IntegerArgument("amount"))
                .executesPlayer((sender, args) -> {
                    ArenaManager arenaManager = this.handler.getArenaManager();
                    Arena arena = null;
                    for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                        for (Arena arena1 : arenaList) {
                            if (arena1.getUuid().equals(args[0])) {
                                arena = arena1;
                            }
                        }
                    }
                    if (arena == null) {
                        sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid", MC.CC.RED.getTextColor()));
                        return;
                    }
                    for (int i = 0; i < (int) args[1]; i++) {
                        Arena arena1 = this.handler.getArenaManager().stackArena(arena, i + 1);
                        if (arena1 == null) {
                            sender.sendMessage(Component.text("There was an error stacking the arena, there might not be enough room to stack it, try a different arena.", MC.CC.RED.getTextColor()));
                            sender.sendMessage(Component.text("A total of " + i + " " + arena.getName() + " arenas were created successfully.", MC.CC.GREEN.getTextColor()));
                            return;
                        }
                        arenaManager.getArenas().get(arena1.getName()).add(arena1);
                        arenaManager.save(arena1);
                        sender.sendMessage(Component.text("Arena " + arena1.getUuid().toString() + " created and saved successfully. (" + i + "/" + args[1] + ")", MC.CC.GREEN.getTextColor()));
                    }
                    sender.sendMessage(Component.text("A total of " + args[1] + " " + arena.getName() + " arenas were created successfully.", MC.CC.GREEN.getTextColor()));
                });
    }

}
