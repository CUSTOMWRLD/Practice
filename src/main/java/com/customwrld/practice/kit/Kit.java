package com.customwrld.practice.kit;

import com.customwrld.customlib.util.InventoryUtil;
import com.customwrld.practice.util.ItemUtil;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class Kit implements ConfigurationSerializable {

    @Getter private ObjectId id;

    @Getter @Setter private boolean enabled;

    @Getter private boolean custom;

    @Getter @Setter private String name;

    @Getter @Setter private ItemStack icon;

    @Getter private boolean combo;

    @Getter private boolean modifyTerrain;

    @Getter private boolean ranked;

    @Getter @Setter private ItemStack[] contents;

    @Getter private List<String> lockedArenas;

    @Getter @Setter private boolean sumo;

    @Getter @Setter private boolean spleef;

    public Kit(String name, ItemStack icon, boolean combo, boolean modifyTerrain, boolean ranked, ItemStack[] contents, boolean custom) {
        this.id = new ObjectId();
        this.enabled = false;
        this.custom = custom;
        this.name = name;
        this.icon = icon;
        this.combo = combo;
        this.modifyTerrain = modifyTerrain;
        this.ranked = ranked;
        this.contents = contents;
        this.lockedArenas = new ArrayList<>();
    }

    public Kit(Document document) {
        try {
            this.id = document.getObjectId("_id");
            this.name = document.getString("name");
            this.enabled = document.getBoolean("enabled");
            this.custom = document.getBoolean("custom");
            this.icon = ItemUtil.itemFrom64(document.getString("icon"));
            this.combo = document.getBoolean("combo");
            this.modifyTerrain = document.getBoolean("modifyTerrain");
            this.ranked = document.getBoolean("ranked");
            this.contents = InventoryUtil.itemStackArrayFromBase64(document.getString("contents"));
            this.lockedArenas = Optional.ofNullable(document.getList("lockedArenas", String.class)).orElse(new ArrayList<>());
            this.sumo = Optional.ofNullable(document.getBoolean("sumo")).orElse(false);
            this.spleef = Optional.ofNullable(document.getBoolean("spleef")).orElse(false);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Kit deserialize(Map<String, Object> args) {
        try {
            return new Kit((String) args.get("name"),
                    ItemUtil.itemFrom64((String) args.get("icon")),
                    (Boolean) args.get("combo"),
                    (Boolean) args.get("modifyTerrain"),
                    (Boolean) args.get("ranked"),
                    InventoryUtil.itemStackArrayFromBase64((String) args.get("contents")),
                    true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("enabled", this.enabled);
        data.put("custom", this.custom);
        data.put("icon", ItemUtil.itemTo64(this.icon));
        data.put("combo", this.combo);
        data.put("modifyTerrain", this.modifyTerrain);
        data.put("ranked", this.ranked);
        data.put("contents", InventoryUtil.itemStackArrayToBase64(this.contents));
        data.put("lockedArenas", this.lockedArenas);
        data.put("sumo", this.sumo);
        data.put("spleef", this.spleef);
        return data;
    }

}
