package com.customwrld.practice.events;

import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.managers.DuelManager;
import com.customwrld.practice.managers.KitManager;
import com.customwrld.practice.player.PracticePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
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
            if (Practice.getPractice().getActiveEvent() != null && player != null) {
                if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                    Practice.getPractice().getActiveEvent().onProjectileHit(event);
                }
            }
            if (player == null) {
                return;
            }
            PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (practicePlayer == null) {
                return;
            }
            DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
            KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
            if (event.getHitBlock() != null && projectile instanceof Snowball) {
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
