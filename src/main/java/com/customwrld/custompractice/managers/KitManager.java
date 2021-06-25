package com.customwrld.custompractice.managers;

import com.customwrld.customlib.util.InventoryUtil;
import com.customwrld.customlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.util.ItemUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KitManager extends Manager {

    @Getter private final Map<String, Kit> kits;

    @Getter private final MongoCollection<Document> kitsCollection;

    public KitManager(ManagerHandler handler) {
        super(handler);
        kits = new HashMap<>();
        kitsCollection = CustomPractice.getDatabase().getCollection("kits");
    }

    public void save(Kit kit) {
        kitsCollection.updateOne(Filters.eq(kit.getId()),
                Updates.combine(
                        Updates.set("name", kit.getName()),
                        Updates.set("enabled", kit.isEnabled()),
                        Updates.set("custom", kit.isCustom()),
                        Updates.set("icon", ItemUtil.itemTo64(kit.getIcon())),
                        Updates.set("combo", kit.isCombo()),
                        Updates.set("modifyTerrain", kit.isModifyTerrain()),
                        Updates.set("ranked", kit.isRanked()),
                        Updates.set("contents", InventoryUtil.itemStackArrayToBase64(kit.getContents())),
                        Updates.set("lockedArenas", kit.getLockedArenas())
                ), new UpdateOptions().upsert(true));
    }

    public void applyKit(Player player, String kit) {
        if (!kits.containsKey(kit)) {
            player.sendMessage(Component.text("There was an error loading your kit.").color(MC.CC.RED.getTextColor()));
            return;
        }
        Kit kit1 = kits.get(kit);
        player.getInventory().setContents(kit1.getContents());
    }

    public Set<Kit> loadAll() {
        Set<Kit> loadedKits = new HashSet<>();
        try (MongoCursor<Document> cursor = kitsCollection.find().cursor()) {
            while (cursor.hasNext()) {
                Kit kit = new Kit(cursor.next());
                if (!kit.isCustom()) {
                    kits.put(kit.getName(), kit);
                }
            }
        }
        return loadedKits;
    }
}
