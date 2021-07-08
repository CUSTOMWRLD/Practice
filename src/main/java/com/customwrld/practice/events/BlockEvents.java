package com.customwrld.practice.events;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.managers.DuelManager;
import com.customwrld.practice.managers.KitManager;
import com.customwrld.practice.player.PracticePlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEvents implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onBlockBreak(event);
            }
        }

        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);

        if (practicePlayer == null) {
            event.setCancelled(true);
            return;
        }

        if (player.hasPermission("custompractice.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (practicePlayer.getState() == PracticePlayer.State.FIGHTING) {
            DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
            KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (kitManager.getKits().get(duel.getKit()).isSpleef() && event.getBlock().getType() == Material.SNOW_BLOCK) {
                    player.getInventory().addItem(new ItemFactory(Material.SNOWBALL).build());
                    player.updateInventory();
                    return;
                }
                if (Practice.getPractice().getManagerHandler().getKitManager().getKits().get(duel.getKit()).isModifyTerrain()) {
                    return;
                }
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onBlockPlace(event);
            }
        }
        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer == null) {
            event.setCancelled(true);
            return;
        }
        if (player.hasPermission("custompractice.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (practicePlayer.getState() == PracticePlayer.State.FIGHTING) {
            DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (Practice.getPractice().getManagerHandler().getKitManager().getKits().get(duel.getKit()).isModifyTerrain()) {
                    return;
                }
            }
        }
        event.setCancelled(true);
    }

}
