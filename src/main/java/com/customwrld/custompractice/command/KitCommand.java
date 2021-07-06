package com.customwrld.custompractice.command;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.commonlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.managers.KitManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class KitCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("adminkit")
                .withPermission("custompractice.admin")
                .withSubcommand(create)
                .withSubcommand(list)
                //.withSubcommand(delete)
                .withSubcommand(enable)
                .withSubcommand(disable)
                .withSubcommand(setIcon)
                .withSubcommand(rename)
                .withSubcommand(give)
                .withSubcommand(setloadout)
                .withSubcommand(listLocked)
                .withSubcommand(lock)
                .withSubcommand(unlock)
                .withSubcommand(sumo)
                .withSubcommand(spleef);
    }

    public static CommandAPICommand create = new CommandAPICommand("create")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("name"))
            .withArguments(new BooleanArgument("combo"))
            .withArguments(new BooleanArgument("modifyTerrain"))
            .withArguments(new BooleanArgument("ranked"))
            .executesPlayer((sender, args) -> {
                Kit kit = new Kit((String) args[0], new ItemFactory(Material.DIAMOND_AXE).setAmount(1).build(), (Boolean) args[1], (Boolean) args[2], (Boolean) args[3], sender.getInventory().getContents(), false);
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                if (kitManager.getKits().containsKey(kit.getName())) {
                    sender.sendMessage(Component.text("There is already a kit with that name, please choose a different one or delete that one!").color(MC.CC.RED.getTextColor()));
                } else {
                    kitManager.save(kit);
                    kitManager.getKits().put(kit.getName(), kit);
                    sender.sendMessage(Component.text("Kit successfully created based on your inventory! Use /adminkit enable <Kit> to enable it.").color(MC.CC.GREEN.getTextColor()));
                }
            });

    public static CommandAPICommand list = new CommandAPICommand("list")
            .withPermission("custompractice.admin")
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    sender.sendMessage(Component.text(kit.getName()).color(kit.isEnabled() ? MC.CC.RED.getTextColor() : MC.CC.GREEN.getTextColor()));
                }
            });

    public static CommandAPICommand enable = new CommandAPICommand("enable")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kit.setEnabled(true);
                        kitManager.save(kit);
                        sender.sendMessage(Component.text(kit.getName() + " has been enabled!").color(MC.CC.GREEN.getTextColor()));
                    }
                }
            });

    public static CommandAPICommand disable = new CommandAPICommand("disable")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kit.setEnabled(false);
                        kitManager.save(kit);
                        sender.sendMessage(Component.text(kit.getName() + " has been disabled!").color(MC.CC.RED.getTextColor()));
                    }
                }
            });

    public static CommandAPICommand setIcon = new CommandAPICommand("setIcon")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .executesPlayer((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            kit.setIcon(sender.getInventory().getItemInMainHand());
                            kitManager.save(kit);
                            sender.sendMessage(Component.text(kit.getName() + " icon has been updated!").color(MC.CC.GREEN.getTextColor()));
                        } else {
                            sender.sendMessage(Component.text("You must be holding an item to set the icon!").color(MC.CC.RED.getTextColor()));
                        }
                    }
                }
            });

    public static CommandAPICommand rename = new CommandAPICommand("rename")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .withArguments(new GreedyStringArgument("new name"))
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        String oldName = kit.getName();
                        kitManager.getKits().remove(oldName);
                        kit.setName((String) args[1]);
                        kitManager.getKits().put(kit.getName(), kit);
                        kitManager.save(kit);
                        sender.sendMessage(Component.text(oldName + " has been renamed to " + kit.getName() + "!").color(MC.CC.GREEN.getTextColor()));
                    }
                }
            });
    public static CommandAPICommand give = new CommandAPICommand("give")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .executesPlayer((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kitManager.applyKit(sender, kit.getName());
                        sender.sendMessage(Component.text("Gave you " + kit.getName() + " kit!").color(MC.CC.GREEN.getTextColor()));
                    }
                }
            });
    public static CommandAPICommand setloadout = new CommandAPICommand("setloadout")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .executesPlayer((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kit.setContents(sender.getInventory().getContents());
                        kitManager.save(kit);
                        sender.sendMessage(Component.text("Set contents of " + kit.getName() + " kit to your inventory!").color(MC.CC.GREEN.getTextColor()));
                    }
                }
            });
    public static CommandAPICommand listLocked = new CommandAPICommand("listlocked")
            .withPermission("custompractice.admin")
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    sender.sendMessage(Component.text(kit.getName() + ": " + kit.getLockedArenas()));
                }
            });
    public static CommandAPICommand lock = new CommandAPICommand("lock")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .withArguments(new StringArgument("arena"))
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        if (!kit.getLockedArenas().contains((String) args[1])) {
                            kit.getLockedArenas().add((String) args[1]);
                            kitManager.save(kit);
                            sender.sendMessage(Component.text("This kit will now only play this arena and any others you have locked the kit to. Remember that these are case sensitive and you also need to lock arenas or else any kit can get them. To remove an arena from this list run /adminkit unlock <kit> <arena>.").color(MC.CC.GREEN.getTextColor()));
                            return;
                        } else {
                            sender.sendMessage(Component.text("This kit is already locked to this arena.").color(MC.CC.RED.getTextColor()));
                            return;
                        }
                    }
                }
                sender.sendMessage(Component.text("Please enter a valid kit!").color(MC.CC.RED.getTextColor()));
            });

    public static CommandAPICommand unlock = new CommandAPICommand("unlock")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .withArguments(new StringArgument("arena"))
            .executes((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        if (kit.getLockedArenas().contains((String) args[1])) {
                            kit.getLockedArenas().remove((String) args[1]);
                            kitManager.save(kit);
                            sender.sendMessage(Component.text("This kit is no longer locked to this arena.").color(MC.CC.GREEN.getTextColor()));
                            return;
                        } else {
                            sender.sendMessage(Component.text("This kit isn't locked to this arena.").color(MC.CC.RED.getTextColor()));
                            return;
                        }
                    }
                }
                sender.sendMessage(Component.text("Please enter a valid kit!").color(MC.CC.RED.getTextColor()));
            });

    public static CommandAPICommand sumo = new CommandAPICommand("sumo")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .withArguments(new BooleanArgument("sumo"))
            .executesPlayer((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kit.setSumo((Boolean) args[1]);
                        kitManager.save(kit);
                        if (kit.isSumo()) {
                            sender.sendMessage(Component.text("This kit is now a sumo kit.").color(MC.CC.GREEN.getTextColor()));
                        } else {
                            sender.sendMessage(Component.text("This kit is no longer a sumo kit.").color(MC.CC.RED.getTextColor()));
                        }
                    }
                }
            });

    public static CommandAPICommand spleef = new CommandAPICommand("spleef")
            .withPermission("custompractice.admin")
            .withArguments(new StringArgument("kit"))
            .withArguments(new BooleanArgument("spleef"))
            .executesPlayer((sender, args) -> {
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                for (Kit kit : kitManager.getKits().values()) {
                    if (kit.getName().equalsIgnoreCase((String) args[0])) {
                        kit.setSpleef((Boolean) args[1]);
                        kitManager.save(kit);
                        if (kit.isSpleef()) {
                            sender.sendMessage(Component.text("This kit is now a spleef kit.").color(MC.CC.GREEN.getTextColor()));
                        } else {
                            sender.sendMessage(Component.text("This kit is no longer a spleef kit.").color(MC.CC.RED.getTextColor()));
                        }
                    }
                }
            });
}
