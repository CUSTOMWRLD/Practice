package com.customwrld.custompractice.managers;

import com.customwrld.customlib.util.Cuboid;
import com.customwrld.customlib.util.WorldEditUtil;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.arena.Arena;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.util.WorldUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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
        CustomPractice.getManagerHandler().getArenaManager().save(arena1);
        return arena1;
    }
}
