package com.customwrld.custompractice.events;

import com.customwrld.customlib.util.ServerUtil;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.KitManager;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.util.ItemUtil;
import com.customwrld.custompractice.util.PlayerUtil;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Set;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        Set<PracticePlayer> profiles = CustomPractice.getManagerHandler().getPracticePlayerManager().load(player);
        if (profiles.isEmpty()) {
            PracticePlayer practicePlayer = new PracticePlayer(player);
            CustomPractice.getManagerHandler().getPracticePlayerManager().save(practicePlayer);
            CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().add(practicePlayer);
        } else {
            CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().addAll(profiles);
        }
        PlayerUtil.resetPlayer(player);
        player.teleport(CustomPractice.getSpawn());
        ItemUtil.setDefaultLoadout(player);
        Bukkit.getOnlinePlayers().forEach(player1 -> ServerUtil.hidePlayer(player, player1));
        ServerUtil.hideServer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
        if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
            Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
            if (duel.getTeamA().contains(player.getUniqueId()) && duel.getTeamA().size() == 1) {
                duel.end(Duel.Result.TEAMB, false);
            } else if (duel.getTeamB().contains(player.getUniqueId()) && duel.getTeamB().size() == 1) {
                duel.end(Duel.Result.TEAMA, false);
            }
        }
        if (practicePlayer != null) {
            CustomPractice.getManagerHandler().getQueueManager().getUnRankedQueue().forEach((s, queue) -> {
                queue.removeFromQueue(practicePlayer);
            });
            CustomPractice.getManagerHandler().getQueueManager().getRankedQueue().forEach((s, queue) -> {
                queue.removeFromQueue(practicePlayer);
            });
            try {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Bukkit.getPlayer(practicePlayer.getUuid()) == null) {
                            CustomPractice.getManagerHandler().getPracticePlayerManager().save(practicePlayer);
                            CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().remove(practicePlayer);
                        }
                    }
                }.runTaskLater(CustomPractice.getCustomPractice(), 20L + 10);
            } catch (Exception e) {
                if (Bukkit.getPlayer(practicePlayer.getUuid()) == null) {
                    CustomPractice.getManagerHandler().getPracticePlayerManager().save(practicePlayer);
                    CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().remove(practicePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.deathMessage(null);
        Player player = event.getEntity();
        Player killer = player.getKiller();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
        if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
            Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
            if (duel.getTeamAAlive().contains(player.getUniqueId())) {
                duel.killPlayerTeamA(player.getName(), player.getUniqueId(), player, practicePlayer, killer);
            } else if (duel.getTeamBAlive().contains(player.getUniqueId())) {
                duel.killPlayerTeamB(player.getName(), player.getUniqueId(), player, practicePlayer, killer);
            }
            //duel.getItemsToClear().addAll(event.getDrops());
        }
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
        KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
        if (practicePlayer.getState() == PracticePlayer.State.FIGHTING) {
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (kitManager.getKits().get(duel.getKit()).isSumo()) {
                    if (duel.getState() == Duel.State.WAITING) {
                        if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (event.getTo().getBlock().getType() == Material.WATER) {
                    if (kitManager.getKits().get(duel.getKit()).isSpleef() || kitManager.getKits().get(duel.getKit()).isSumo()) {
                        if (duel.getTeamAAlive().contains(player.getUniqueId())) {
                            if (duel.getState() != Duel.State.ENDED) {
                                player.getWorld().strikeLightningEffect(player.getLocation());
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
                            }
                            duel.killPlayerTeamA(player.getName(), player.getUniqueId(), player, practicePlayer, null);
                        }
                        if (duel.getTeamBAlive().contains(player.getUniqueId())) {
                            if (duel.getState() != Duel.State.ENDED) {
                                player.getWorld().strikeLightningEffect(player.getLocation());
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
                            }
                            duel.killPlayerTeamB(player.getName(), player.getUniqueId(), player, practicePlayer, null);
                        }
                    }
                }
            }
        }
        if (event.getTo().getY() < 0) {
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING) {
                player.setFallDistance(0);
                player.teleport(CustomPractice.getSpawn());
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            /*
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (practicePlayer.getState() == PracticePlayer.State.LOBBY) {
                    event.setCancelled(true);
                    assert player != null;
                    player.setFallDistance(0);
                    player.teleport(CustomPractice.getSpawn());
                }
            }*/
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING) {
                event.setCancelled(true);
                return;
            }
            DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
            KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
            assert player != null;
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                if (kitManager.getKits().get(duel.getKit()).isSumo() || kitManager.getKits().get(duel.getKit()).isSpleef()) {
                    event.setDamage(0);
                }
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            Player damager = null;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            }
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING) {
                event.setCancelled(true);
            }
            if (damager != null) {
                PracticePlayer practiceDamager = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(damager);
                if (practiceDamager.getState() != PracticePlayer.State.FIGHTING) {
                    event.setCancelled(true);
                }
            }
            assert player != null;
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
                if (kitManager.getKits().containsKey(duel.getKit())) {
                    Kit kit = kitManager.getKits().get(duel.getKit());
                    if (kit.isSpleef()) {
                        if (!(event.getDamager() instanceof Projectile)) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (practicePlayer != null) {
                if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHungerLost(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (practicePlayer != null) {
                if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer != null) {
            if (practicePlayer.getState() == PracticePlayer.State.SPECTATING) {
                ItemUtil.setSpectatorLoadout(player);
            }
        }
    }
}
