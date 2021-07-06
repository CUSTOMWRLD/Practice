package com.customwrld.custompractice.managers;

import com.customwrld.commonlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.arena.Arena;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.duel.Request;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.queue.Queue;
import com.customwrld.custompractice.util.ItemUtil;
import com.customwrld.custompractice.util.PlayerUtil;
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
        Arena arena = CustomPractice.getManagerHandler().getArenaManager().getRandomArena(request.getKit());
        QueueManager queueManager = CustomPractice.getManagerHandler().getQueueManager();
        Player A = Bukkit.getPlayer(playerA.getUuid());
        Player B = Bukkit.getPlayer(playerA.getUuid());
        if (arena == null) {
            //arena = CustomPractice.getManagerHandler().getArenaManager().stackRandomArena();
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
