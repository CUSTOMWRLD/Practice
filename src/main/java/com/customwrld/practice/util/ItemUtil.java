package com.customwrld.practice.util;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.commonlib.util.MC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemUtil {

    // Default Loadout START
    public static ItemStack UNRANKED = new ItemFactory(Material.IRON_SWORD).setName(Component.text("Unranked").decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to join an unranked queue!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack RANKED = new ItemFactory(Material.DIAMOND_SWORD).setName(Component.text("Ranked", MC.CC.BLUE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to join a ranked queue!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack CUSTOM = new ItemFactory(Material.GOLDEN_SWORD).setName(Component.text("Custom", MC.CC.GOLD.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to duel someone using a custom kit!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack STATS = new ItemFactory(Material.PLAYER_HEAD).setName(Component.text("Stats", MC.CC.LIGHT_PURPLE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to view your stats!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack KIT_EDITOR = new ItemFactory(Material.BOOK).setName(Component.text("Kit Editor", MC.CC.GOLD.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to create a custom kit or edit an existing one!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack EVENT = new ItemFactory(Material.EMERALD).setName(Component.text("Event", MC.CC.GREEN.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to join an event!", MC.CC.GRAY.getTextColor())).build();
    // Default Loadout END

    // Party Loadout START
    public static ItemStack PARTY_FFA = new ItemFactory(Material.IRON_SWORD).setName(Component.text("Party Fight", MC.CC.LIGHT_PURPLE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to start a teams or FFA duel with your party!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack PARTY_FIGHT = new ItemFactory(Material.NETHERITE_SWORD).setName(Component.text("Fight Other Parties", MC.CC.LIGHT_PURPLE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to duel another party!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack PARTY_EVENT = new ItemFactory(Material.EMERALD).setName(Component.text("Party Event", MC.CC.LIGHT_PURPLE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to start an event with your party!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack PARTY_INFO = new ItemFactory(Material.PAPER).setName(Component.text("Party Info", MC.CC.LIGHT_PURPLE.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to view info about this party!", MC.CC.GRAY.getTextColor())).build();
    public static ItemStack LEAVE_PARTY = new ItemFactory(Material.BARRIER).setName(Component.text("Leave Party", MC.CC.RED.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to leave this party!", MC.CC.GRAY.getTextColor())).build();
    // Party Loadout END

    // Spectator Loadout START
    //public static ItemStack LEAVE_SPECTATING = new ItemFactory(Material.BARRIER).setName(CC.BOLD + CC.RED + "Leave Spectator Mode").setLore(Component.text("Right click to leave spectator mode!").build();
    public static ItemStack LEAVE_SPECTATING = new ItemFactory(Material.BARRIER).setName(Component.text("Leave Spectator Mode", MC.CC.RED.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to leave spectator mode!", MC.CC.GRAY.getTextColor())).build();
    // Spectator Loadout END

    // Queue Loadout START
    public static ItemStack LEAVE_QUEUE = new ItemFactory(Material.BARRIER).setName(Component.text("Leave Queue", MC.CC.RED.getTextColor()).decoration(TextDecoration.BOLD, true)).setLore(Component.text("Right click to leave the queue!", MC.CC.GRAY.getTextColor())).build();
    // Queue Loadout END

    // Misc START
    public static ItemStack EVENT_PLACEHOLDER = new ItemFactory(Material.EMERALD).build();
    // Misc END

    public static void setDefaultLoadout(Player player) {
        Inventory inventory = player.getInventory();
        inventory.setItem(0, UNRANKED);
        inventory.setItem(1, RANKED);
        inventory.setItem(2, CUSTOM);
        ItemStack playerStats = STATS.clone();
        SkullMeta skullMeta = (SkullMeta) playerStats.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        playerStats.setItemMeta(skullMeta);
        inventory.setItem(4, playerStats);
        inventory.setItem(7, KIT_EDITOR);
        inventory.setItem(8, EVENT);
        player.updateInventory();
    }

    public static void setPartyLoadout(Player player) {
        Inventory inventory = player.getInventory();
        inventory.setItem(0, PARTY_FFA);
        inventory.setItem(1, PARTY_FIGHT);
        inventory.setItem(2, PARTY_EVENT);
        inventory.setItem(4, PARTY_INFO);
        inventory.setItem(8, LEAVE_PARTY);
        player.updateInventory();
    }

    public static void setSpectatorLoadout(Player player) {
        Inventory inventory = player.getInventory();
        inventory.setItem(8, LEAVE_SPECTATING);
        player.updateInventory();
    }

    public static void setQueueLoadout(Player player) {
        Inventory inventory = player.getInventory();
        inventory.setItem(8, LEAVE_QUEUE);
        player.updateInventory();
    }

    public static String itemTo64(ItemStack stack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public static ItemStack itemFrom64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
