package com.customwrld.custompractice.events;

import com.customwrld.customlib.util.InventoryUtil;
import com.customwrld.customlib.util.MC;
import com.customwrld.customlib.util.ServerUtil;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.gui.UnrankedGui;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.queue.Queue;
import com.customwrld.custompractice.util.ItemUtil;
import com.customwrld.custompractice.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class InventoryEvents implements Listener {

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer != null) {
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (player.hasPermission("custompractice.admin") && player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (practicePlayer != null) {
                if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer != null) {
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
        if (practicePlayer != null) {
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    if (event.getItem() == null || event.getHand() != EquipmentSlot.HAND || event.getHand() == EquipmentSlot.OFF_HAND || event.getItem().getItemMeta() == null) {
                        return;
                    }
                    if (!player.hasPermission("custompractice.admin") && player.getGameMode() != GameMode.CREATIVE) {
                        event.setUseInteractedBlock(Event.Result.DENY);
                        event.setUseItemInHand(Event.Result.DENY);
                    }
                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.UNRANKED) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        new UnrankedGui(player, practicePlayer, null);
                    }

                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.RANKED) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        //TODO Open ranked queue gui
                    }

                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.CUSTOM) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        //TODO Open custom kit queue gui
                    }

                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.STATS) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        //TODO Open stats gui
                    }

                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.LEAVE_QUEUE) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        Queue queue = CustomPractice.getManagerHandler().getQueueManager().getQueue(practicePlayer);
                        if (queue != null) {
                            queue.removeFromQueue(practicePlayer);
                            practicePlayer.setState(PracticePlayer.State.LOBBY);
                            PlayerUtil.resetPlayer(player);
                            ItemUtil.setDefaultLoadout(player);
                            player.sendMessage(Component.text("You have left the queue!").color(MC.CC.RED.getTextColor()));
                        }
                    }
                    if (InventoryUtil.isSimilar(itemStack, ItemUtil.LEAVE_SPECTATING) && event.getHand() == EquipmentSlot.HAND) {
                        event.setCancelled(true);
                        DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
                        for (Duel duel : duelManager.getActiveDuels().values()) {
                            if (duel.getSpectators().contains(practicePlayer.getUuid())) {
                                duel.getSpectators().remove(practicePlayer.getUuid());
                                PlayerUtil.resetPlayer(player);
                                player.setGameMode(GameMode.SURVIVAL);
                                ServerUtil.hideServer(player);
                                player.teleport(CustomPractice.getSpawn());
                                practicePlayer.setState(PracticePlayer.State.LOBBY);
                                ItemUtil.setDefaultLoadout(player);
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println(player.getName() + "'s practiceplayer was null");
        }
    }
}
