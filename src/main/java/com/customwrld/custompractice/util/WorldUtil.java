package com.customwrld.custompractice.util;

import com.customwrld.customlib.util.Cuboid;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;

public class WorldUtil {

    public static World WORLD = Bukkit.getWorld("world");

    public static class EmptyChunkGenerator extends ChunkGenerator {
        @Override
        public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biome) {
            return createChunkData(world);
        }
    }

    public static void pasteSchematicAt(World world, File folder, String child, Location location) {
        File file = new File(folder, child);

        if (!file.exists()) {
            Bukkit.getLogger().log(Level.SEVERE, "The schematic " + child + " could not be located, please make sure it exists!");
            return;
        }

        try {
            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(file);

            assert format != null;
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            }

            System.out.println(location);

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSchematic(World world, File folder, String child, Cuboid cuboid) {
        File file = new File(folder, child);

        try {
            CuboidRegion region = new CuboidRegion(BlockVector3.at(cuboid.getLowerNE().getBlockX(), cuboid.getLowerNE().getBlockY(), cuboid.getLowerNE().getBlockZ()),
                    BlockVector3.at(cuboid.getUpperSW().getBlockX(), cuboid.getUpperSW().getBlockY(), cuboid.getUpperSW().getBlockZ()));
            Clipboard clipboard = new BlockArrayClipboard(region);

            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1);

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            forwardExtentCopy.setCopyingEntities(true);
            Operations.complete(forwardExtentCopy);
            
            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            }
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
    }
}
