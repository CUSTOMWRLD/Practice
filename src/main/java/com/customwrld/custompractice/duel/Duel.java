package com.customwrld.custompractice.duel;

import com.customwrld.customlib.util.MC;
import com.customwrld.customlib.util.ServerUtil;
import com.customwrld.customlib.util.WorldEditUtil;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.arena.Arena;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.util.ItemUtil;
import com.customwrld.custompractice.util.PlayerUtil;
import com.customwrld.custompractice.util.WorldUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Duel {

    @Getter private Arena arena;

    @Getter private String kit;

    @Getter private UUID uuid;

    @Getter
    private boolean ranked;

    @Getter @Setter private State state;

    @Getter private Type type;

    @Getter
    private UUID teamALeader;

    @Getter
    private UUID teamBLeader;

    @Getter
    private Set<UUID> teamA;

    @Getter
    private Set<UUID> teamB;

    @Getter
    private Set<UUID> teamAAlive;

    @Getter
    private Set<UUID> teamBAlive;

    @Getter
    private Set<UUID> spectators;

    @Getter
    private BlockArrayClipboard clipboard;

    @Getter
    private final long timestamp;

    public Duel(Arena arena, String kit, UUID uuid, boolean ranked, UUID teamALeader, UUID teamBLeader, Set<UUID> teamA, Set<UUID> teamB) {
        this.state = State.INIT;
        this.type = Type.NORMAL;
        this.spectators = new HashSet<>();
        this.arena = arena;
        this.kit = kit;
        this.uuid = uuid;
        this.ranked = ranked;
        this.teamALeader = teamALeader;
        this.teamBLeader = teamBLeader;
        this.teamA = teamA;
        this.teamB = teamB;
        this.teamAAlive = new HashSet<>(teamA);
        this.teamBAlive = new HashSet<>(teamB);
        this.timestamp = System.currentTimeMillis();
    }

    public Duel(Arena arena, String kit, UUID uuid, UUID teamALeader, Set<UUID> teamA) {
        this.state = State.INIT;
        this.type = Type.FFA;
        this.spectators = new HashSet<>();
        this.arena = arena;
        this.kit = kit;
        this.uuid = uuid;
        this.teamALeader = teamALeader;
        this.teamA = teamA;
        this.teamAAlive = new HashSet<>(teamA);
        this.timestamp = System.currentTimeMillis();
    }

    public void init() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : teamA) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                teamA.remove(uuid);
                teamAAlive.remove(uuid);
                break;
            }
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            practicePlayer.setState(PracticePlayer.State.WAITING);
            ServerUtil.showServer(player);
            PlayerUtil.resetPlayer(player);
            player.teleport(arena.getTeamALocation());
            players.add(player);
            CustomPractice.getManagerHandler().getKitManager().applyKit(player, kit);
        }
        for (UUID uuid : teamB) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                teamB.remove(uuid);
                teamBAlive.remove(uuid);
                break;
            }
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);
            practicePlayer.setState(PracticePlayer.State.WAITING);
            ServerUtil.showServer(player);
            PlayerUtil.resetPlayer(player);
            player.teleport(arena.getTeamBLocation());
            players.add(player);
            CustomPractice.getManagerHandler().getKitManager().applyKit(player, kit);
        }

        if (CustomPractice.getManagerHandler().getKitManager().getKits().get(kit).isModifyTerrain()) {
            CuboidRegion region = new CuboidRegion(BlockVector3.at(arena.getCuboid().getLowerNE().getBlockX(), arena.getCuboid().getLowerNE().getBlockY(), arena.getCuboid().getLowerNE().getBlockZ()),
                    BlockVector3.at(arena.getCuboid().getUpperSW().getBlockX(), arena.getCuboid().getUpperSW().getBlockY(), arena.getCuboid().getUpperSW().getBlockZ()));
            this.clipboard = new BlockArrayClipboard(region);
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(WorldUtil.WORLD), -1)) {
                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        editSession, region, clipboard, region.getMinimumPoint()
                );
                Operations.complete(forwardExtentCopy);
            } catch (WorldEditException e) {
                this.end(Result.DRAW, true);
                e.printStackTrace();
            }
        }

        this.state = State.WAITING;
        AtomicInteger countdown = new AtomicInteger(5);
        BukkitRunnable duelTask = (new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown.get() != 0) {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 1);
                        player.sendMessage(Component.text().append(Component.text("Match starting in ").color(MC.CC.WHITE.getTextColor())).append(Component.text(countdown.get()).color(MC.CC.BLUE.getTextColor())).append(Component.text("...").color(MC.CC.WHITE.getTextColor())));
                    }
                    countdown.getAndDecrement();
                } else {
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 2);
                        player.sendMessage(Component.text("Match started!").color(MC.CC.WHITE.getTextColor()));
                        CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player).setState(PracticePlayer.State.FIGHTING);
                    }
                    this.cancel();
                    state = State.STARTED;
                }
            }
        });
        duelTask.runTaskTimer(CustomPractice.getCustomPractice(), 20L, 20L);
        
        
    }

    public void end(Result result, boolean force) {

        this.state = State.ENDED;

        Map<UUID, String> winningTeam = null;
        Map<UUID, String> loosingTeam = null;

        BukkitRunnable duelEndTask = new BukkitRunnable() {
            @Override
            public void run() {
                endTask();
            }
        };

        switch (result) {
            case TEAMA:
                winningTeam = new HashMap<>();
                for (UUID uuid1 : teamA) {
                    winningTeam.put(uuid1, Bukkit.getOfflinePlayer(uuid1).getName());
                }
                loosingTeam = new HashMap<>();
                for (UUID uuid1 : teamB) {
                    loosingTeam.put(uuid1, Bukkit.getOfflinePlayer(uuid1).getName());
                }
                break;
            case TEAMB:
                winningTeam = new HashMap<>();
                for (UUID uuid1 : teamB) {
                    winningTeam.put(uuid1, Bukkit.getOfflinePlayer(uuid1).getName());
                }
                loosingTeam = new HashMap<>();
                for (UUID uuid1 : teamA) {
                    loosingTeam.put(uuid1, Bukkit.getOfflinePlayer(uuid1).getName());
                }
                break;
            default:
                break;
        }
        if (winningTeam == null) {
            for (UUID uuid : teamA) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(Component.text("Draw!").color(MC.CC.RED.getTextColor()));
                }
            }
            for (UUID uuid : teamB) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(Component.text("Draw!").color(MC.CC.RED.getTextColor()));
                }
            }
            for (UUID uuid : spectators) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && !teamA.contains(uuid) && !teamB.contains(uuid)) {
                    player.sendMessage(Component.text("Draw!").color(MC.CC.RED.getTextColor()));
                }
            }
            if (force) {
                endTask();
            } else {
                duelEndTask.runTaskLater(CustomPractice.getCustomPractice(), 20L * 5);
            }
            return;
        }

        for (Map.Entry<UUID, String> entry : winningTeam.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(entry.getKey());
            if (player != null) {
                player.sendMessage(Component.text().append(Component.text("Winners: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", winningTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
                player.sendMessage(Component.text().append(Component.text("Losers: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", loosingTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
                practicePlayer.setUnrankedWins(practicePlayer.getUnrankedWins() + 1);
            }
        }
        for (Map.Entry<UUID, String> entry : loosingTeam.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(entry.getKey());
            if (player != null) {
                player.sendMessage(Component.text().append(Component.text("Winners: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", winningTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
                player.sendMessage(Component.text().append(Component.text("Losers: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", loosingTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
            }
            practicePlayer.setUnrankedLosses(practicePlayer.getUnrankedLosses() + 1);
        }
        for (UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !teamA.contains(uuid) && !teamB.contains(uuid)) {
                ServerUtil.showServer(player);
                player.sendMessage(Component.text().append(Component.text("Winners: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", winningTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
                player.sendMessage(Component.text().append(Component.text("Losers: ").color(MC.CC.YELLOW.getTextColor())).append(Component.text(String.join(", ", loosingTeam.values())).color(MC.CC.WHITE.getTextColor())).append(Component.text()).append(Component.text("").color(MC.CC.YELLOW.getTextColor())));
            }
        }
        if (force) {
            endTask();
        } else {
            duelEndTask.runTaskLater(CustomPractice.getCustomPractice(), 20L * 5);
        }
    }

    public void destroy() {
        arena.setInUse(false);
        CustomPractice.getManagerHandler().getDuelManager().getPlayerToDuel().entrySet().removeIf(uuiduuidEntry -> uuiduuidEntry.getValue().equals(uuid));
        CustomPractice.getManagerHandler().getDuelManager().getActiveDuels().remove(uuid);
    }

    public Set<UUID> getOpponent(UUID uuid) {
        if (teamA.contains(uuid)) {
            return teamB;
        } else {
            return teamA;
        }
    }

    public void killPlayerTeamA(String name, UUID uuid, Player player, PracticePlayer practicePlayer, Player killer) {
        this.teamAAlive.remove(uuid);
        for (UUID uuid1 : teamA) {
            Player player1 = Bukkit.getPlayer(uuid1);
            if (player1 != null) {
                if (killer != null) {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.GREEN.getTextColor())).append(Component.text(" was slain by ").color(MC.CC.WHITE.getTextColor())).append(Component.text(killer.getName()).color(MC.CC.RED.getTextColor())));
                } else {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.GREEN.getTextColor())).append(Component.text(" died.").color(MC.CC.WHITE.getTextColor())));
                }
            }
        }
        for (UUID uuid1 : teamB) {
            Player player1 = Bukkit.getPlayer(uuid1);
            if (player1 != null) {
                if (killer != null) {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.RED.getTextColor())).append(Component.text(" was slain by ").color(MC.CC.WHITE.getTextColor())).append(Component.text(killer.getName()).color(MC.CC.GREEN.getTextColor())));
                } else {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.RED.getTextColor())).append(Component.text(" died.").color(MC.CC.WHITE.getTextColor())));
                }
            }
        }
        this.addSpectator(name, uuid, player, practicePlayer);
        if (teamAAlive.isEmpty()) {
            this.end(Result.TEAMB, false);
        }
    }

    public void killPlayerTeamB(String name, UUID uuid, Player player, PracticePlayer practicePlayer, Player killer) {
        this.teamBAlive.remove(uuid);
        //this.teamBAlive.remove(name, uuid);
        for (UUID uuid1 : teamA) {
            Player player1 = Bukkit.getPlayer(uuid1);
            if (player1 != null) {
                if (killer != null) {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.RED.getTextColor())).append(Component.text(" was slain by ").color(MC.CC.WHITE.getTextColor())).append(Component.text(killer.getName()).color(MC.CC.GREEN.getTextColor())));
                } else {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.RED.getTextColor())).append(Component.text(" died.").color(MC.CC.WHITE.getTextColor())));
                }
            }
        }
        for (UUID uuid1 : teamB) {
            Player player1 = Bukkit.getPlayer(uuid1);
            if (player1 != null) {
                if (killer != null) {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.GREEN.getTextColor())).append(Component.text(" was slain by ").color(MC.CC.WHITE.getTextColor())).append(Component.text(killer.getName()).color(MC.CC.RED.getTextColor())));
                } else {
                    player1.sendMessage(Component.text().append(Component.text(name).color(MC.CC.GREEN.getTextColor())).append(Component.text(" died.").color(MC.CC.WHITE.getTextColor())));
                }
            }
        }
        this.addSpectator(name, uuid, player, practicePlayer);
        if (teamBAlive.isEmpty()) {
            this.end(Result.TEAMA, false);
        }
    }

    public void addSpectator(String name, UUID uuid, Player player, PracticePlayer practicePlayer) {
        practicePlayer.setState(PracticePlayer.State.SPECTATING);
        PlayerUtil.resetPlayer(player);
        this.spectators.add(uuid);
        for (UUID uuid1 : teamAAlive) {
            try {
                ServerUtil.hidePlayer(player, Objects.requireNonNull(Bukkit.getPlayer(uuid1)));
                ServerUtil.showPlayer(Bukkit.getPlayer(uuid1), player);
            } catch (NullPointerException ignore) {
            }
        }
        for (UUID uuid1 : teamBAlive) {
            try {
                ServerUtil.hidePlayer(player, Objects.requireNonNull(Bukkit.getPlayer(uuid1)));
                ServerUtil.showPlayer(Bukkit.getPlayer(uuid1), player);
            } catch (NullPointerException ignore) {
            }
        }
        for (UUID uuid1 : spectators) {
            try {
                ServerUtil.showPlayer(player, Objects.requireNonNull(Bukkit.getPlayer(uuid1)));
            } catch (NullPointerException ignore) {
            }
        }
        ItemUtil.setSpectatorLoadout(player);
        player.setGameMode(GameMode.CREATIVE);
    }

    public enum State {
        INIT,
        WAITING,
        STARTED,
        ENDED
    }

    public enum Type {
        NORMAL,
        FFA
    }

    public enum Result {
        TEAMA,
        TEAMB,
        DRAW
    }

    public void endTask() {
        for (UUID uuid : teamA) {
            Player player = Bukkit.getPlayer(uuid);
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(uuid);
            if (player != null) {
                PlayerUtil.resetPlayer(player);
                ServerUtil.hideServer(player);
                player.teleport(CustomPractice.getSpawn());
                practicePlayer.setState(PracticePlayer.State.LOBBY);
                ItemUtil.setDefaultLoadout(player);
            }
        }
        for (UUID uuid : teamB) {
            Player player = Bukkit.getPlayer(uuid);
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(uuid);
            if (player != null) {
                PlayerUtil.resetPlayer(player);
                ServerUtil.hideServer(player);
                player.teleport(CustomPractice.getSpawn());
                practicePlayer.setState(PracticePlayer.State.LOBBY);
                ItemUtil.setDefaultLoadout(player);
            }
        }
        for (UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);
            PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(uuid);
            if (player != null) {
                PlayerUtil.resetPlayer(player);
                player.setGameMode(GameMode.SURVIVAL);
                ServerUtil.hideServer(player);
                player.teleport(CustomPractice.getSpawn());
                practicePlayer.setState(PracticePlayer.State.LOBBY);
                ItemUtil.setDefaultLoadout(player);
            }
        }
        for (Entity entity : WorldUtil.WORLD.getEntities()) {
            if (!(entity instanceof Player)) {
                if (arena.getCuboid().contains(entity)) {
                    entity.remove();
                }
            }
        }
        /*
        if (CustomPractice.getManagerHandler().getKitManager().getKits().get(kit).isModifyTerrain()) {
            WorldEditUtil.pasteSchematic();
        }*/
        if (clipboard != null && CustomPractice.getManagerHandler().getKitManager().getKits().get(kit).isModifyTerrain()) {
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(WorldUtil.WORLD), -1)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(clipboard.getMinimumPoint())
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
        destroy();
    }
}
