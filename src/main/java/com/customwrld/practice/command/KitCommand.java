package com.customwrld.practice.command;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.kit.Kit;
import com.customwrld.practice.managers.KitManager;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class KitCommand extends Command {

    private final ManagerHandler handler;

    public KitCommand() {
        this.handler = Practice.getPractice().getManagerHandler();
        this.command = "adminkit";
        this.permission = "practice.admin";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withPermission(this.permission)
                .withSubcommand(this.create())
                .withSubcommand(this.list())
                .withSubcommand(this.enable())
                .withSubcommand(this.disable())
                .withSubcommand(this.setIcon())
                .withSubcommand(this.rename())
                .withSubcommand(this.give())
                .withSubcommand(this.setLoadout())
                .withSubcommand(this.listLocked())
                .withSubcommand(this.lock())
                .withSubcommand(this.unlock())
                .withSubcommand(this.sumo())
                .withSubcommand(this.spleef());
    }

    private CommandAPICommand create() {
        return new CommandAPICommand("create")
            .withArguments(new StringArgument("name"))
            .withArguments(new BooleanArgument("combo"))
            .withArguments(new BooleanArgument("modifyTerrain"))
            .withArguments(new BooleanArgument("ranked"))
            .executesPlayer((sender, args) -> {
                Kit kit = new Kit((String) args[0], new ItemFactory(Material.DIAMOND_AXE).build(), (Boolean) args[1], (Boolean) args[2], (Boolean) args[3], sender.getInventory().getContents(), false);
                KitManager kitManager = this.handler.getKitManager();
                if (kitManager.getKits().containsKey(kit.getName())) {
                    sender.sendMessage(Component.text("There is already a kit with that name, please choose a different one or delete that one!", MC.CC.RED.getTextColor()));
                } else {
                    kitManager.save(kit);
                    kitManager.getKits().put(kit.getName(), kit);
                    sender.sendMessage(Component.text("Kit successfully created based on your inventory! Use /adminkit enable <Kit> to enable it.", MC.CC.GREEN.getTextColor()));
                }
            });
    }

    private CommandAPICommand list() {
        return new CommandAPICommand("list")
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        sender.sendMessage(Component.text(kit.getName(), kit.isEnabled() ? MC.CC.RED.getTextColor() : MC.CC.GREEN.getTextColor()));
                    }
                });
    }

    private CommandAPICommand enable() {
        return new CommandAPICommand("enable")
                .withArguments(new StringArgument("kit"))
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            kit.setEnabled(true);
                            kitManager.save(kit);
                            sender.sendMessage(Component.text(kit.getName() + " has been enabled!", MC.CC.GREEN.getTextColor()));
                        }
                    }
                });
    }

    private CommandAPICommand disable() {
        return new CommandAPICommand("disable")
                .withArguments(new StringArgument("kit"))
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            kit.setEnabled(false);
                            kitManager.save(kit);
                            sender.sendMessage(Component.text(kit.getName() + " has been disabled!", MC.CC.RED.getTextColor()));
                        }
                    }
                });
    }

    private CommandAPICommand setIcon() {
        return new CommandAPICommand("seticon")
                .withArguments(new StringArgument("kit"))
                .executesPlayer((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                kit.setIcon(sender.getInventory().getItemInMainHand());
                                kitManager.save(kit);
                                sender.sendMessage(Component.text(kit.getName() + " icon has been updated!", MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("You must be holding an item to set the icon!", MC.CC.RED.getTextColor()));
                            }
                        }
                    }
                });
    }

    private CommandAPICommand rename() {
        return new CommandAPICommand("rename")
                .withArguments(new StringArgument("kit"))
                .withArguments(new GreedyStringArgument("new name"))
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            String oldName = kit.getName();
                            kitManager.getKits().remove(oldName);
                            kit.setName((String) args[1]);
                            kitManager.getKits().put(kit.getName(), kit);
                            kitManager.save(kit);
                            sender.sendMessage(Component.text(oldName + " has been renamed to " + kit.getName() + "!", MC.CC.GREEN.getTextColor()));
                        }
                    }
                });
    }

    private CommandAPICommand give() {
        return new CommandAPICommand("give")
                .withArguments(new StringArgument("kit"))
                .executesPlayer((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            kitManager.applyKit(sender, kit.getName());
                            sender.sendMessage(Component.text("Gave you " + kit.getName() + " kit!", MC.CC.GREEN.getTextColor()));
                        }
                    }
                });
    }

    private CommandAPICommand setLoadout() {
        return new CommandAPICommand("setloadout")
                .withArguments(new StringArgument("kit"))
                .executesPlayer((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            kit.setContents(sender.getInventory().getContents());
                            kitManager.save(kit);
                            sender.sendMessage(Component.text("Set contents of " + kit.getName() + " kit to your inventory!", MC.CC.GREEN.getTextColor()));
                        }
                    }
                });
    }

    private CommandAPICommand listLocked() {
        return new CommandAPICommand("listlocked")
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        sender.sendMessage(Component.text(kit.getName() + ": " + kit.getLockedArenas()));
                    }
                });
    }

    private CommandAPICommand lock() {
        return new CommandAPICommand("lock")
                .withArguments(new StringArgument("kit"))
                .withArguments(new StringArgument("arena"))
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            if (!kit.getLockedArenas().contains((String) args[1])) {
                                kit.getLockedArenas().add((String) args[1]);
                                kitManager.save(kit);
                                sender.sendMessage(Component.text("This kit will now only play this arena and any others you have locked the kit to. Remember that these are case sensitive and you also need to lock arenas or else any kit can get them. To remove an arena from this list run /adminkit unlock <kit> <arena>.", MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("This kit is already locked to this arena.", MC.CC.RED.getTextColor()));
                            }
                            return;
                        }
                    }
                    sender.sendMessage(Component.text("Please enter a valid kit!", MC.CC.RED.getTextColor()));
                });
    }

    private CommandAPICommand unlock() {
        return new CommandAPICommand("unlock")
                .withArguments(new StringArgument("kit"))
                .withArguments(new StringArgument("arena"))
                .executes((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            if (kit.getLockedArenas().contains((String) args[1])) {
                                kit.getLockedArenas().remove((String) args[1]);
                                kitManager.save(kit);
                                sender.sendMessage(Component.text("This kit is no longer locked to this arena.", MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("This kit isn't locked to this arena.", MC.CC.RED.getTextColor()));
                            }
                            return;
                        }
                    }
                    sender.sendMessage(Component.text("Please enter a valid kit!", MC.CC.RED.getTextColor()));
                });
    }

        private CommandAPICommand sumo() {
            return new CommandAPICommand("sumo")
                    .withArguments(new StringArgument("kit"))
                    .withArguments(new BooleanArgument("sumo"))
                    .executesPlayer((sender, args) -> {
                        KitManager kitManager = this.handler.getKitManager();
                        for (Kit kit : kitManager.getKits().values()) {
                            if (kit.getName().equalsIgnoreCase((String) args[0])) {
                                kit.setSumo((Boolean) args[1]);
                                kitManager.save(kit);
                                if (kit.isSumo()) {
                                    sender.sendMessage(Component.text("This kit is now a sumo kit.", MC.CC.GREEN.getTextColor()));
                                } else {
                                    sender.sendMessage(Component.text("This kit is no longer a sumo kit.", MC.CC.RED.getTextColor()));
                                }
                            }
                        }
                    });
        }

    private CommandAPICommand spleef() {
        return new CommandAPICommand("spleef")
                .withArguments(new StringArgument("kit"))
                .withArguments(new BooleanArgument("spleef"))
                .executesPlayer((sender, args) -> {
                    KitManager kitManager = this.handler.getKitManager();
                    for (Kit kit : kitManager.getKits().values()) {
                        if (kit.getName().equalsIgnoreCase((String) args[0])) {
                            kit.setSpleef((Boolean) args[1]);
                            kitManager.save(kit);
                            if (kit.isSpleef()) {
                                sender.sendMessage(Component.text("This kit is now a spleef kit.", MC.CC.GREEN.getTextColor()));
                            } else {
                                sender.sendMessage(Component.text("This kit is no longer a spleef kit.", MC.CC.RED.getTextColor()));
                            }
                        }
                    }
                });
    }

}
