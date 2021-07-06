package com.customwrld.custompractice.events;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.KitManager;
import com.customwrld.custompractice.player.PracticePlayer;
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
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer == null) {
            event.setCancelled(true);
            return;
        }
        if (player.hasPermission("custompractice.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (practicePlayer.getState() == PracticePlayer.State.FIGHTING) {
            DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
            KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (kitManager.getKits().get(duel.getKit()).isSpleef() && event.getBlock().getType() == Material.SNOW_BLOCK) {
                    player.getInventory().addItem(new ItemFactory(Material.SNOWBALL).build());
                    player.updateInventory();
                    return;
                }
                if (CustomPractice.getManagerHandler().getKitManager().getKits().get(duel.getKit()).isModifyTerrain()) {
                    return;
                }
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer == null) {
            event.setCancelled(true);
            return;
        }
        if (player.hasPermission("custompractice.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (practicePlayer.getState() == PracticePlayer.State.FIGHTING) {
            DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (CustomPractice.getManagerHandler().getKitManager().getKits().get(duel.getKit()).isModifyTerrain()) {
                    return;
                }
            }
        }
        event.setCancelled(true);
    }

}
