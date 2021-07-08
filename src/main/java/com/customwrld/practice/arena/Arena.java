package com.customwrld.practice.arena;

import com.customwrld.customlib.util.CollectionsUtil;
import com.customwrld.customlib.util.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Location;

import java.util.*;

public class Arena {

    @Getter @Setter private boolean enabled;

    @Getter private ObjectId id;

    @Getter private UUID uuid;

    @Getter private String name;

    @Getter @Setter private Location teamALocation;

    @Getter @Setter private Location teamBLocation;

    @Getter @Setter private Cuboid cuboid;

    @Getter @Setter private boolean inUse;

    @Getter @Setter private List<String> lockedKits;

    public Arena(String name, Location teamALocation, Location teamBLocation) {
        this.id = new ObjectId();
        this.enabled = false;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.teamALocation = teamALocation;
        this.teamBLocation = teamBLocation;
        this.cuboid = new Cuboid(teamALocation, teamBLocation);
        this.lockedKits = new ArrayList<>();
    }

    public Arena(Document document) {
        try {
            this.id = document.getObjectId("_id");
            this.uuid = UUID.fromString(document.getString("uuid"));
            this.name = document.getString("name");
            this.enabled = document.getBoolean("enabled");
            this.teamALocation = Location.deserialize(CollectionsUtil.genericMapToType(document.get("teamALocation", Map.class), String.class, Object.class));
            this.teamBLocation = Location.deserialize(CollectionsUtil.genericMapToType(document.get("teamBLocation", Map.class), String.class, Object.class));
            this.cuboid = new Cuboid(CollectionsUtil.genericMapToType(document.get("cuboid", Map.class), String.class, Object.class));
            this.lockedKits = Optional.ofNullable(document.getList("lockedKits", String.class)).orElse(new ArrayList<>());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
