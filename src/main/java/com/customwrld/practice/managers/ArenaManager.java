package com.customwrld.practice.managers;

import com.customwrld.customlib.util.Cuboid;
import com.customwrld.practice.Practice;
import com.customwrld.practice.arena.Arena;
import com.customwrld.practice.kit.Kit;
import com.customwrld.practice.util.WorldUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManager extends Manager {

    @Getter private final Map<String, List<Arena>> arenas;

    @Getter private final MongoCollection<Document> arenaCollection;

    @Getter private BlockArrayClipboard clipboard;

    public ArenaManager(ManagerHandler managerHandler) {
        super(managerHandler);
        arenas = new HashMap<>();
        arenaCollection = Practice.getPractice().getDatabase().getCollection("arena");

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
        List<Arena> arenaList = arenas.values().stream().flatMap(Collection::stream).filter(arena -> !arena.isInUse()).collect(Collectors.toList());
        KitManager kitManager = Practice.getPractice().getManagerHandler().getKitManager();
        Kit kit1 = kitManager.getKits().get(kit);
        if (!kit1.getLockedArenas().isEmpty()) {
            arenaList = arenaList.stream().filter(arena -> kit1.getLockedArenas().contains(arena.getName())).collect(Collectors.toList());
        }
        Arena arena = arenaList.get(ThreadLocalRandom.current().nextInt(arenaList.size()));
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

    public Arena uuidToArena(UUID uuid) {
        for (List<Arena> arenaList : arenas.values()) {
            for (Arena arena : arenaList) {
                if (arena.getUuid().equals(uuid)) {
                    return arena;
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

    public Arena stackRandomArena() {
        Object[] value = arenas.values().toArray();
        List<Arena> arenaList = (List<Arena>) value[ThreadLocalRandom.current().nextInt(value.length)];
        Arena arena = arenaList.get(arenaList.size()-1);
        Arena arena1 = new Arena(arena.getName(), arena.getTeamALocation().add(500, 0, 0), arena.getTeamBLocation().add(500, 0, 0));
        arena1.setCuboid(arena.getCuboid().clone().shift(Cuboid.CuboidDirection.EAST, 500));
        arena1.setLockedKits(new ArrayList<>(arena.getLockedKits()));
        arena1.setEnabled(arena.isEnabled());

        Chunk chunk = WorldUtil.WORLD.getChunkAt(arena1.getCuboid().getCenter());
        for (int x = chunk.getX() - 10; x < chunk.getX() + 10; x++) {
            for (int z = chunk.getZ() - 10; z < chunk.getZ() + 10; z++) {
                Chunk chunk1 = WorldUtil.WORLD.getChunkAt(x, z);
                if (!WorldUtil.WORLD.isChunkGenerated(x, z)) {
                    WorldUtil.WORLD.loadChunk(chunk1);
                }
            }
        }

        WorldUtil.pasteSchematicAt(WorldUtil.WORLD, Bukkit.getPluginManager().getPlugin("WorldEdit").getDataFolder(), "schematics/" + arena.getName() +".schem", arena1.getCuboid().getLowerNE());

        /*
        CuboidRegion region = new CuboidRegion(BlockVector3.at(arena.getCuboid().getLowerNE().getBlockX(), arena.getCuboid().getLowerNE().getBlockY(), arena.getCuboid().getLowerNE().getBlockZ()),
                BlockVector3.at(arena.getCuboid().getUpperSW().getBlockX(), arena.getCuboid().getUpperSW().getBlockY(), arena.getCuboid().getUpperSW().getBlockZ()));
        this.clipboard = new BlockArrayClipboard(region);
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(WorldUtil.WORLD), -1)) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, BlockVector3.at(arena1.getCuboid().getLowerNE().getBlockX(), arena1.getCuboid().getLowerNE().getBlockY(), arena1.getCuboid().getLowerNE().getBlockZ())//region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            return null;
        }*/
        Practice.getPractice().getManagerHandler().getArenaManager().save(arena1);
        return arena1;
    }

    public Arena stackArena(Arena arena, int i) {
        if (arena == null) {
            return null;
        }
        for (List<Arena> arenaList : arenas.values()) {
            for (Arena arena1 : arenaList) {
                if (arena1.getCuboid() != null) {
                    if (arena1.getCuboid().contains(arena.getTeamALocation().clone().add(500 * i, 0, 0)) || arena1.getCuboid().contains(arena.getTeamBLocation().clone().add(500 * i, 0, 0))) {
                        return null;
                    }
                }
            }
        }
        Arena arena1 = new Arena(arena.getName(), arena.getTeamALocation().clone().add(500 * i, 0, 0), arena.getTeamBLocation().clone().add(500 * i, 0, 0));
        arena1.setCuboid(arena.getCuboid().clone().shift(Cuboid.CuboidDirection.SOUTH, 500 * i));
        arena1.setLockedKits(new ArrayList<>(arena.getLockedKits()));
        arena1.setEnabled(arena.isEnabled());

        Chunk chunk = WorldUtil.WORLD.getChunkAt(arena1.getCuboid().getCenter());
        for (int x = chunk.getX() - 10; x < chunk.getX() + 10; x++) {
            for (int z = chunk.getZ() - 10; z < chunk.getZ() + 10; z++) {
                Chunk chunk1 = WorldUtil.WORLD.getChunkAt(x, z);
                if (!WorldUtil.WORLD.isChunkLoaded(chunk1)) {
                    WorldUtil.WORLD.loadChunk(chunk1);
                }
            }
        }

        WorldUtil.pasteSchematicAt(WorldUtil.WORLD, Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldEdit")).getDataFolder(), "schematics/" + arena.getName() +".schem", arena1.getCuboid().getLowerNE());

        Practice.getPractice().getManagerHandler().getArenaManager().save(arena1);
        return arena1;
    }
}
