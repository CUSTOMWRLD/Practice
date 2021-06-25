package com.customwrld.custompractice.managers;

import com.customwrld.customlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Request;
import com.customwrld.custompractice.player.PracticePlayer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PracticePlayerManager extends Manager {

    @Getter
    private final Set<PracticePlayer> players;

    @Getter
    private final MongoCollection<Document> playerCollection;

    public PracticePlayerManager(ManagerHandler handler) {
        super(handler);
        this.players = new HashSet<>();
        this.playerCollection = CustomPractice.getDatabase().getCollection("profiles");
    }

    public PracticePlayer getPracticePlayer(Player player) {
        for (PracticePlayer practicePlayer : players) {
            if (practicePlayer.getUuid().equals(player.getUniqueId())) {
                return practicePlayer;
            }
        }
        return null;
    }

    public PracticePlayer getPracticePlayer(UUID uuid) {
        for (PracticePlayer practicePlayer : players) {
            if (practicePlayer.getUuid().equals(uuid)) {
                return practicePlayer;
            }
        }
        return null;
    }

    public void sendDuelRequest(PracticePlayer sender, PracticePlayer target, Player player, Request request) {
        target.getActiveRequests().put(sender.getUuid(), request);
        Component component = Component.text(sender.getName() + " has sent you a duel request, click here to accept.").color(MC.CC.AQUA.getTextColor()).clickEvent(ClickEvent.runCommand("/accept " + sender.getName()));
        player.sendMessage(component);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CustomPractice.getCustomPractice(), () -> {
            target.getActiveRequests().remove(sender.getUuid());
        }, 20L * 15);
    }

    public void save(PracticePlayer practicePlayer) {
        playerCollection.updateOne(Filters.eq(practicePlayer.getId()),
                Updates.combine(
                        Updates.set("uuid", practicePlayer.getUuid().toString()),
                        Updates.set("name", practicePlayer.getName()),
                        Updates.set("rankedwins", practicePlayer.getRankedWins()),
                        Updates.set("rankedlosses", practicePlayer.getRankedLosses()),
                        Updates.set("unrankedwins", practicePlayer.getUnrankedWins()),
                        Updates.set("unrankedlosses", practicePlayer.getUnrankedWins())//,
                        //Updates.set("customkits", practicePlayer.getCustomKits().values().stream().map(Kit::serialize).collect(Collectors.toList()))
                ), new UpdateOptions().upsert(true));
    }

    public Set<PracticePlayer> load(Player player) {
        Set<PracticePlayer> loadedProfiles = new HashSet<>();
        try (MongoCursor<Document> cursor = playerCollection.find(
                Filters.eq("uuid", player.getUniqueId().toString())
        ).cursor()) {
            while (cursor.hasNext()) {
                PracticePlayer practicePlayer = new PracticePlayer(cursor.next());
                loadedProfiles.add(practicePlayer);
            }
        }
        return loadedProfiles;
    }
}
