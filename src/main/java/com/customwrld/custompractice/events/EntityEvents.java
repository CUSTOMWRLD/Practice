package com.customwrld.custompractice.events;

import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.KitManager;
import com.customwrld.custompractice.player.PracticePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EntityEvents implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();

        if (projectileSource instanceof Player) {
            Player player = ((Player) projectileSource).getPlayer();
            if (player == null) {
                return;
            }
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (practicePlayer == null) {
                return;
            }
            DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
            KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
            if (event.getHitBlock() != null) {
                if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                    Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                    if (kitManager.getKits().get(duel.getKit()).isSpleef() && event.getHitBlock().getType() == Material.SNOW_BLOCK) {
                        event.getHitBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}
