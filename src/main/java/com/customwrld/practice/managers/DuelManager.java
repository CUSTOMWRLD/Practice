package com.customwrld.practice.managers;

import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.arena.Arena;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.duel.Request;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.queue.Queue;
import com.customwrld.practice.util.ItemUtil;
import com.customwrld.practice.util.PlayerUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class DuelManager extends Manager {

    @Getter private final Map<UUID, Duel> activeDuels;
    @Getter private final Map<UUID, UUID> playerToDuel;

    public DuelManager(ManagerHandler handler) {
        super(handler);
        activeDuels = new HashMap<>();
        playerToDuel = new HashMap<>();
    }

    public void startUnrankedDuel(PracticePlayer playerA, PracticePlayer playerB, Request request) {
        UUID uuid = UUID.randomUUID();
        Arena arena = Practice.getPractice().getManagerHandler().getArenaManager().getRandomArena(request.getKit());
        QueueManager queueManager = Practice.getPractice().getManagerHandler().getQueueManager();
        Player A = Bukkit.getPlayer(playerA.getUuid());
        Player B = Bukkit.getPlayer(playerA.getUuid());
        if (arena == null) {
            //arena = CustomPractice.getCustomPractice().getManagerHandler().getArenaManager().stackRandomArena();
            if (A != null) {
                A.sendMessage(Component.text("There are no available arenas, please wait for one to be open and tell staff to make more.").color(MC.CC.RED.getTextColor()));
                Queue queue = queueManager.getQueue(playerA);
                queue.removeFromQueue(playerA);
                playerA.setState(PracticePlayer.State.LOBBY);
                PlayerUtil.resetPlayer(A);
                ItemUtil.setDefaultLoadout(A);
                A.sendMessage(Component.text("You have left the queue!").color(MC.CC.RED.getTextColor()));
            }
            if (B != null) {
                B.sendMessage(Component.text("There are no available arenas, please wait for one to be open and tell staff to make more.").color(MC.CC.RED.getTextColor()));
                Queue queue = queueManager.getQueue(playerB);
                queue.removeFromQueue(playerB);
                playerB.setState(PracticePlayer.State.LOBBY);
                PlayerUtil.resetPlayer(B);
                ItemUtil.setDefaultLoadout(B);
                B.sendMessage(Component.text("You have left the queue!").color(MC.CC.RED.getTextColor()));
            }
            return;
        }
        Duel duel = new Duel(arena,
                request.getKit(),
                uuid,
                false,
                playerA.getUuid(),
                playerB.getUuid(),
                new HashSet<>(Collections.singletonList(playerA.getUuid())),
                new HashSet<>(Collections.singletonList(playerB.getUuid())));
        activeDuels.put(uuid, duel);
        playerToDuel.put(playerA.getUuid(), uuid);
        playerToDuel.put(playerB.getUuid(), uuid);
        duel.init();
    }
}
