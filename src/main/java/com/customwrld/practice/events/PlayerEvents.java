package com.customwrld.practice.events;

import com.customwrld.customlib.util.ServerUtil;
import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.kit.Kit;
import com.customwrld.practice.managers.DuelManager;
import com.customwrld.practice.managers.KitManager;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.ItemUtil;
import com.customwrld.practice.util.PlayerUtil;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        Set<PracticePlayer> profiles = Practice.getPractice().getManagerHandler().getPracticePlayerManager().load(player);
        if (profiles.isEmpty()) {
            PracticePlayer practicePlayer = new PracticePlayer(player);
            Practice.getPractice().getManagerHandler().getPracticePlayerManager().save(practicePlayer);
            Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPlayers().add(practicePlayer);
        } else {
            Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPlayers().addAll(profiles);
        }
        PlayerUtil.resetPlayer(player);
        player.teleport(Practice.getPractice().getSpawn());
        ItemUtil.setDefaultLoadout(player);
        Bukkit.getOnlinePlayers().forEach(player1 -> ServerUtil.hidePlayer(player, player1));
        ServerUtil.hideServer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onQuit(event);
            }
        }
        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
        if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
            Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
            if (duel.getTeamA().contains(player.getUniqueId()) && duel.getTeamA().size() == 1) {
                duel.end(Duel.Result.TEAMB, false);
            } else if (duel.getTeamB().contains(player.getUniqueId()) && duel.getTeamB().size() == 1) {
                duel.end(Duel.Result.TEAMA, false);
            }
        }
        if (practicePlayer != null) {
            Practice.getPractice().getManagerHandler().getQueueManager().getUnRankedQueue().forEach((s, queue) -> {
                queue.removeFromQueue(practicePlayer);
            });
            Practice.getPractice().getManagerHandler().getQueueManager().getRankedQueue().forEach((s, queue) -> {
                queue.removeFromQueue(practicePlayer);
            });
            try {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Bukkit.getPlayer(practicePlayer.getUuid()) == null) {
                            Practice.getPractice().getManagerHandler().getPracticePlayerManager().save(practicePlayer);
                            Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPlayers().remove(practicePlayer);
                        }
                    }
                }.runTaskLater(Practice.getPractice(), 20L + 10);
            } catch (Exception e) {
                if (Bukkit.getPlayer(practicePlayer.getUuid()) == null) {
                    Practice.getPractice().getManagerHandler().getPracticePlayerManager().save(practicePlayer);
                    Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPlayers().remove(practicePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.deathMessage(null);
        Player player = event.getEntity();
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onDeath(event);
            }
        }
        Player killer = player.getKiller();
        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
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
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onMove(event);
            }
        }
        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
        KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
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
                player.teleport(Practice.getPractice().getSpawn());
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = ((Player) event.getEntity()).getPlayer();
            if (Practice.getPractice().getActiveEvent() != null && player != null) {
                if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                    Practice.getPractice().getActiveEvent().onDamage(event);
                }
            }
            PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            /*
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (practicePlayer.getState() == PracticePlayer.State.LOBBY) {
                    event.setCancelled(true);
                    assert player != null;
                    player.setFallDistance(0);
                    player.teleport(CustomPractice.getCustomPractice().getSpawn());
                }
            }*/
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING) {
                event.setCancelled(true);
                return;
            }
            DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
            KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
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
            if (Practice.getPractice().getActiveEvent() != null && player != null) {
                if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                    Practice.getPractice().getActiveEvent().onAttack(event);
                }
            }
            Player damager = null;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            }
            PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            DuelManager duelManager = Practice.getPractice().getManagerHandler().getDuelManager();
            if (practicePlayer.getState() != PracticePlayer.State.FIGHTING) {
                event.setCancelled(true);
            }
            if (damager != null) {
                PracticePlayer practiceDamager = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(damager);
                if (practiceDamager.getState() != PracticePlayer.State.FIGHTING) {
                    event.setCancelled(true);
                }
            }
            assert player != null;
            if (duelManager.getActiveDuels().containsKey(duelManager.getPlayerToDuel().get(player.getUniqueId()))) {
                Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
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
        if (event.getEntity() instanceof Player player) {
            if (Practice.getPractice().getActiveEvent() != null) {
                if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                    Practice.getPractice().getActiveEvent().onPickup(event);
                }
            }
            PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            if (practicePlayer != null) {
                if (practicePlayer.getState() != PracticePlayer.State.FIGHTING && practicePlayer.getState() != PracticePlayer.State.EVENT) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHungerLost(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Practice.getPractice().getActiveEvent() != null) {
                if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                    Practice.getPractice().getActiveEvent().onHungerLost(event);
                }
            }
            PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
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
        if (Practice.getPractice().getActiveEvent() != null) {
            if (Practice.getPractice().getActiveEvent().isInEvent(player)) {
                Practice.getPractice().getActiveEvent().onPostRespawn(event);
            }
        }
        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
        if (practicePlayer != null) {
            if (practicePlayer.getState() == PracticePlayer.State.SPECTATING) {
                ItemUtil.setSpectatorLoadout(player);
            }
        }
    }
}
