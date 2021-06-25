package com.customwrld.custompractice.command;

import com.customwrld.customlib.util.Cuboid;
import com.customwrld.customlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.arena.Arena;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.managers.ArenaManager;
import com.customwrld.custompractice.managers.KitManager;
import com.customwrld.custompractice.util.WorldUtil;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.regions.Region;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArenaCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("arena")
                .withPermission("custompractice.admin")
                .withSubcommand(create)
                .withSubcommand(list)
                .withSubcommand(setlocation)
                .withSubcommand(enable)
                .withSubcommand(disable)
                .withSubcommand(setRegion)
                .withSubcommand(listLocked)
                .withSubcommand(lock)
                .withSubcommand(unlock);
    }

    public static CommandAPICommand create = new CommandAPICommand("create")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("name"))
            .executesPlayer((sender, args) -> {
                Arena arena = new Arena((String) args[0], sender.getLocation(), sender.getLocation());
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                if (!arenaManager.getArenas().containsKey(arena.getName())) {
                    arenaManager.getArenas().put(arena.getName(), new ArrayList<>());
                } else {
                    arenaManager.getArenas().get(arena.getName()).add(arena);
                    arena.setLockedKits(new ArrayList<>(arenaManager.getArenas().get(arena.getName()).get(0).getLockedKits()));
                }
                arenaManager.save(arena);
                Component message = Component.text("Arena successfully created, click here to copy the uuid.").clickEvent(ClickEvent.copyToClipboard(arena.getUuid().toString()));
                sender.sendMessage(message);
            });

    public static CommandAPICommand list = new CommandAPICommand("list")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("name"))
            .executesPlayer((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                if (!arenaManager.getArenas().containsKey((String) args[0])) {
                    for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                        sender.sendMessage(entry.getKey() + " | " + entry.getValue().size());
                    }
                }
                for (Arena arena : arenaManager.getArenas().get((String) args[0])) {
                    Component message = Component.text(arena.getUuid().toString()).clickEvent(ClickEvent.copyToClipboard(arena.getUuid().toString()));
                    Component message1 = Component.text("Click to teleport");
                    if (arena.getTeamALocation() != null) {
                        message1.hoverEvent(HoverEvent.showText(Component.text("Teleport to Team A spawn").color(TextColor.color(0,0,255))));
                        message1.clickEvent(ClickEvent.runCommand("/tp " + arena.getTeamALocation().getBlockX() + " " + arena.getTeamALocation().getBlockY() + " " + arena.getTeamALocation().getBlockZ()));
                    } else if (arena.getTeamBLocation() != null) {
                        message1.hoverEvent(HoverEvent.showText(Component.text("Teleport to Team B spawn").color(TextColor.color(0,0,255))));
                        message1.clickEvent(ClickEvent.runCommand("/tp " + arena.getTeamBLocation().getBlockX() + " " + arena.getTeamBLocation().getBlockY() + " " + arena.getTeamBLocation().getBlockZ()));
                    }
                    sender.sendMessage(message);
                    sender.sendMessage(message1);
                }
            });

    public static CommandAPICommand setlocation = new CommandAPICommand("setlocation")
            .withPermission("custompractice.admin")
            .withArguments(new UUIDArgument("uuid"))
            .withArguments(new StringArgument("A/B"))
            .executesPlayer((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                Arena arena = null;
                for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                    for (Arena arena1 : arenaList) {
                        if (arena1.getUuid().equals(args[0])) {
                            arena = arena1;
                        }
                    }
                }
                if (arena == null) {
                    sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid").color(MC.CC.RED.getTextColor()));
                    return;
                }
                if (((String) args[1]).equalsIgnoreCase("a")) {
                    arena.setTeamALocation(sender.getLocation());
                } else if (((String) args[1]).equalsIgnoreCase("b")) {
                    arena.setTeamBLocation(sender.getLocation());
                } else {
                    sender.sendMessage(Component.text("Please provide either A or B").color(MC.CC.RED.getTextColor()));
                    return;
                }
                sender.sendMessage(Component.text("Set location for team " + args[1] + "!").color(MC.CC.GREEN.getTextColor()));
                arenaManager.save(arena);
            });

    public static CommandAPICommand enable = new CommandAPICommand("enable")
            .withPermission("custompractice.admin")
            .withArguments(new UUIDArgument("uuid"))
            .executesPlayer((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                Arena arena = null;
                for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                    for (Arena arena1 : arenaList) {
                        if (arena1.getUuid().equals(args[0])) {
                            arena = arena1;
                        }
                    }
                }
                if (arena == null) {
                    sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid").color(MC.CC.RED.getTextColor()));
                    return;
                }
                arena.setEnabled(true);
                arenaManager.save(arena);
            });

    public static CommandAPICommand disable = new CommandAPICommand("disable")
            .withPermission("custompractice.admin")
            .withArguments(new UUIDArgument("uuid"))
            .executesPlayer((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                Arena arena = null;
                for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                    for (Arena arena1 : arenaList) {
                        if (arena1.getUuid().equals(args[0])) {
                            arena = arena1;
                        }
                    }
                }
                if (arena == null) {
                    sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid").color(MC.CC.RED.getTextColor()));
                    return;
                }
                arena.setEnabled(false);
                arenaManager.save(arena);
            });

    public static CommandAPICommand setRegion = new CommandAPICommand("setRegion")
            .withPermission("custompractice.admin")
            .withArguments(new UUIDArgument("uuid"))
            .executesPlayer((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                Arena arena = null;
                for (List<Arena> arenaList : arenaManager.getArenas().values()) {
                    for (Arena arena1 : arenaList) {
                        if (arena1.getUuid().equals(args[0])) {
                            arena = arena1;
                        }
                    }
                }
                if (arena == null) {
                    sender.sendMessage(Component.text("Could not locate the arena you provided, please check the uuid!").color(MC.CC.RED.getTextColor()));
                    return;
                }
                Region region;
                try {
                    region = CustomPractice.getWorldEditPlugin().getSession(sender).getSelection();
                } catch (IncompleteRegionException e) {
                    sender.sendMessage(Component.text("Please make a WorldEdit selection first!").color(MC.CC.RED.getTextColor()));
                    return;
                }
                if (region == null) {
                    sender.sendMessage(Component.text("There was an error trying to get the region!").color(MC.CC.RED.getTextColor()));
                    return;
                }
                arena.setCuboid(new Cuboid(new Location(WorldUtil.WORLD, region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ()),
                        new Location(WorldUtil.WORLD, region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ())));
                arenaManager.save(arena);
                sender.sendMessage(Component.text("Region has beeen successfully updated!").color(MC.CC.GREEN.getTextColor()));
            });
    public static CommandAPICommand listLocked = new CommandAPICommand("listlocked")
            .withPermission("custompractice.admin")
            .executes((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                    sender.sendMessage(Component.text(entry.getKey() + ": " + entry.getValue().get(0).getLockedKits()));
                }
            });
    public static CommandAPICommand lock = new CommandAPICommand("lock")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("arena"))
            .withArguments(new StringArgument("kit"))
            .executes((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase((String) args[0])) {
                        for (Arena arena : entry.getValue()) {
                            if (!arena.getLockedKits().contains((String) args[1])) {
                                arena.getLockedKits().add((String) args[1]);
                                arenaManager.save(arena);
                                sender.sendMessage(Component.text("Arena " + arena.getUuid().toString() + " has successfully been locked to this kit.").color(MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("This arena is already locked to this kit.").color(MC.CC.RED.getTextColor()));
                            }
                        }
                    }
                }
                //sender.sendMessage(Component.text("Please enter a valid kit!").color(MC.CC.RED.getTextColor()));
            });

    public static CommandAPICommand unlock = new CommandAPICommand("lock")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("arena"))
            .withArguments(new StringArgument("kit"))
            .executes((sender, args) -> {
                ArenaManager arenaManager = CustomPractice.getManagerHandler().getArenaManager();
                for (Map.Entry<String, List<Arena>> entry : arenaManager.getArenas().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase((String) args[0])) {
                        for (Arena arena : entry.getValue()) {
                            if (arena.getLockedKits().contains((String) args[1])) {
                                arena.getLockedKits().remove((String) args[1]);
                                arenaManager.save(arena);
                                sender.sendMessage(Component.text("Arena has successfully been unlocked from arena " + arena.getUuid().toString() + ".").color(MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("This arena isn't locked to this kit.").color(MC.CC.RED.getTextColor()));
                            }
                        }
                    }
                }
                sender.sendMessage(Component.text("Please enter a valid kit!").color(MC.CC.RED.getTextColor()));
            });
}
