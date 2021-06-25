package com.customwrld.custompractice.player;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Request;
import com.customwrld.custompractice.kit.Kit;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PracticePlayer {

    @Getter
    private ObjectId id;

    @Getter
    private UUID uuid;

    @Getter
    private String name;

    @Getter
    @Setter
    private State state;

    @Getter
    @Setter
    private int rankedWins;

    @Getter
    @Setter
    private int rankedLosses;

    @Getter
    @Setter
    private int unrankedWins;

    @Getter
    @Setter
    private int unrankedLosses;

    @Getter
    private Map<UUID, Request> activeRequests;

    @Getter
    private Map<String, Kit> customKits;

    public PracticePlayer(Player player) {
        this.id = new ObjectId();
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.state = State.LOBBY;
        this.rankedWins = 0;
        this.rankedLosses = 0;
        this.unrankedWins = 0;
        this.unrankedLosses = 0;
        this.activeRequests = new HashMap<>();
        this.customKits = new HashMap<>();
        CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().add(this);
    }

    public PracticePlayer(Document document) {
        try {
            this.state = State.LOADING;
            this.id = document.getObjectId("_id");
            this.uuid = UUID.fromString(document.getString("uuid"));
            this.name = document.getString("name");
            this.rankedWins = document.getInteger("rankedwins");
            this.rankedLosses = document.getInteger("rankedlosses");
            this.rankedWins = document.getInteger("unrankedwins");
            this.unrankedLosses = document.getInteger("unrankedlosses");
            this.activeRequests = new HashMap<>();
            /*
            this.customKits = (Map<String, Kit>) document.getList("customkits", Map.class).stream().map(obj -> Kit.deserialize((Map<String, Object>) obj));
            for (Map.Entry<String, Kit> entry : this.customKits.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    this.customKits.remove(entry.getKey());
                }
            }*/
            CustomPractice.getManagerHandler().getPracticePlayerManager().getPlayers().add(this);
            this.state = State.LOBBY;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public enum State {
        LOADING,
        LOBBY,
        QUEUE,
        WAITING,
        FIGHTING,
        SPECTATING,
        EVENT,
        EDITING
    }

}
