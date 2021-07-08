package com.customwrld.custompractice.event;

import com.customwrld.customlib.util.Cuboid;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.player.PracticePlayer;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Event {

    @Getter @Setter private State state;
    @Getter private final String name;
    @Getter private String icon;
    @Getter private Cuboid arena;
    @Getter @Setter private PracticePlayer host;
    @Getter private final Map<UUID, PracticePlayer> players;
    @Getter private int taskID;

    public Event(String name) {
        this.state = State.INACTIVE;
        this.players = new HashMap<>();
        this.name = name;

    }

    public void onQuit(PlayerQuitEvent event) {
    }

    public void onMove(PlayerMoveEvent event) {
    }

    public void onDeath(PlayerDeathEvent event) {
    }

    public void onDamage(EntityDamageEvent event) {
    }

    public void onAttack(EntityDamageByEntityEvent event) {
    }

    public void onPostRespawn(PlayerPostRespawnEvent event) {
    }

    public void onHungerLost(FoodLevelChangeEvent event) {
    }

    public void onPickup(EntityPickupItemEvent event) {
    }

    public void onBlockBreak(BlockBreakEvent event) {
    }

    public void onBlockPlace(BlockPlaceEvent event) {
    }

    public void onProjectileHit(ProjectileHitEvent event) {
    }

    public void onSwapHands(PlayerSwapHandItemsEvent event) {
    }

    public void onInventoryInteract(InventoryClickEvent event) {
    }

    public void onDrop(PlayerDropItemEvent event) {
    }

    public void onInteract(PlayerInteractEvent event) {
    }

    public enum State {
        INACTIVE,
        INIT,
        WAITING,
        STARTED,
        ENDED
    }

    public void destroy() {
        host = null;
        CustomPractice.getCustomPractice().setActiveEvent(null);
    }

    public boolean isInEvent(Player player) {
        return players.containsKey(player.getUniqueId());
    }
}
