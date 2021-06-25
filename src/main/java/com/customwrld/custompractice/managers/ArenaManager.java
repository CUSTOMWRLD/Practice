package com.customwrld.custompractice.managers;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.arena.Arena;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManager extends Manager {

    @Getter
    private final Map<String, List<Arena>> arenas;

    @Getter
    private final MongoCollection<Document> arenaCollection;

    public ArenaManager(ManagerHandler managerHandler) {
        super(managerHandler);
        arenas = new HashMap<>();
        arenaCollection = CustomPractice.getDatabase().getCollection("arena");

    }

    public void save(Arena arena) {
        arenaCollection.updateOne(Filters.eq(arena.getId()),
                Updates.combine(
                        Updates.set("uuid", arena.getUuid().toString()),
                        Updates.set("name", arena.getName()),
                        Updates.set("enabled", arena.isEnabled()),
                        Updates.set("teamALocation", arena.getTeamALocation().serialize()),
                        Updates.set("teamBLocation", arena.getTeamBLocation().serialize()),
                        Updates.set("cuboid", arena.getCuboid().serialize()),
                        Updates.set("lockedKits", arena.getLockedKits())
                ), new UpdateOptions().upsert(true));
    }

    public Arena getRandomArena() {
        List<Arena> arenaList = arenas.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Arena arena = arenaList.get(ThreadLocalRandom.current().nextInt(arenaList.size()));
        for (int i = 0; i < 20; i++) {
            if (!arena.isInUse()) {
                arena.setInUse(true);
                return arena;
            }
        }
        return null;
    }

    public Arena getRandomArena(String kit) {
        List<Arena> arenaList = arenas.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Arena arena = arenaList.get(ThreadLocalRandom.current().nextInt(arenaList.size()));
        KitManager kitManager = CustomPractice.getManagerHandler().getKitManager();
        for (int i = 0; i < 20; i++) {
            if (arena.getLockedKits().isEmpty() || arena.getLockedKits().contains(kit)) {
                if (kitManager.getKits().get(kit).getLockedArenas().isEmpty() || kitManager.getKits().get(kit).getLockedArenas().contains(arena.getName())) {
                    if (!arena.isInUse()) {
                        arena.setInUse(true);
                        return arena;
                    }
                }
            }
        }
        return null;
    }

    public Arena getNamedArena(String name) {
        if (!arenas.containsKey(name)) {
            return getRandomArena();
        }
        List<Arena> arenaList = arenas.get(name);
        return arenaList.get(ThreadLocalRandom.current().nextInt(arenaList.size()));
    }

    public Set<Arena> loadAll() {
        Set<Arena> loadedArenas = new HashSet<>();
        try (MongoCursor<Document> cursor = arenaCollection.find().cursor()) {
            while (cursor.hasNext()) {
                Arena arena = new Arena(cursor.next());
                if (!arenas.containsKey(arena.getName())) {
                    arenas.put(arena.getName(), new ArrayList<>());
                }
                arenas.get(arena.getName()).add(arena);
            }
        }
        return loadedArenas;
    }

}
