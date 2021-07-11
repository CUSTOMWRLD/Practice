package com.customwrld.practice.event;

import com.customwrld.customlib.util.CollectionsUtil;
import com.customwrld.customlib.util.Cuboid;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.practice.Practice;
import com.customwrld.practice.managers.EventManager;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.ItemUtil;
import com.customwrld.practice.util.WorldUtil;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class Event {

    private State state;
    private final String name;
    private ItemStack icon;
    private Cuboid arena;
    private PracticePlayer host;
    private final Map<UUID, PracticePlayer> players;
    private int taskID;
    private EventManager eventManager;

    public Event(String name) {
        this.eventManager = Practice.getPractice().getManagerHandler().getEventManager();
        this.state = State.INACTIVE;
        this.players = new HashMap<>();
        this.name = name;
        this.load();
    }

    public void load() {
        try (MongoCursor<Document> cursor = this.eventManager.getEventCollection().find().cursor()) {
            boolean found = false;
            while (cursor.hasNext()) {
                Document document = cursor.next();
                if (document.getString("name") != null) {
                    if (document.getString("name").equals(this.name)) {
                        found = true;
                        this.icon = Optional.ofNullable(ItemUtil.itemFrom64(document.getString("icon"))).orElse(ItemUtil.EVENT_PLACEHOLDER);
                        this.arena = Optional.of(new Cuboid(CollectionsUtil.genericMapToType(document.get("arena", Map.class), String.class, Object.class))).orElse(new Cuboid(WorldUtil.WORLD, 0, 0, 0, 0, 0, 0));
                        this.extendLoad(document);
                    }
                }
            }
            if (!found) {
                this.icon = ItemUtil.EVENT_PLACEHOLDER;
                this.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        this.eventManager.getEventCollection().updateOne(Filters.eq("name", this.name),
                Updates.combine(
                        Updates.set("name", this.name),
                        Updates.set("icon", ItemUtil.itemTo64(this.icon)),
                        Updates.set("arena", this.arena.serialize()),
                        extendSave()
                ), new UpdateOptions().upsert(true));
    }

    private void extendLoad(Document document) {
    }

    private Bson extendSave() {
        return Updates.combine();
    }

    public void onQuit(PlayerQuitEvent event) {}

    public void onMove(PlayerMoveEvent event) {}

    public void onDeath(PlayerDeathEvent event) {}

    public void onDamage(EntityDamageEvent event) {}

    public void onAttack(EntityDamageByEntityEvent event) {}

    public void onPostRespawn(PlayerPostRespawnEvent event) {}

    public void onHungerLost(FoodLevelChangeEvent event) {}

    public void onPickup(EntityPickupItemEvent event) {}

    public void onBlockBreak(BlockBreakEvent event) {}

    public void onBlockPlace(BlockPlaceEvent event) {}

    public void onProjectileHit(ProjectileHitEvent event) {}

    public void onSwapHands(PlayerSwapHandItemsEvent event) {}

    public void onInventoryInteract(InventoryClickEvent event) {}

    public void onDrop(PlayerDropItemEvent event) {}

    public void onInteract(PlayerInteractEvent event) {}

    public enum State {
        INACTIVE,
        INIT,
        WAITING,
        STARTED,
        ENDED
    }

    public void destroy() {
        this.host = null;
        this.players.clear();
        this.state = State.INACTIVE;
        Practice.getPractice().setActiveEvent(null);
    }

    public boolean isInEvent(Player player) {
        return players.containsKey(player.getUniqueId());
    }

}
