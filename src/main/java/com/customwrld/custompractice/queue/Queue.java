package com.customwrld.custompractice.queue;

import com.customwrld.commonlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Request;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.util.ItemUtil;
import com.customwrld.custompractice.util.PlayerUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Queue {

    @Getter
    private final Kit kit;

    @Getter
    private final boolean ranked;

    @Getter
    private final List<PracticePlayer> playersInQueue;

    public Queue(Kit kit) {
        this.kit = kit;
        this.ranked = kit.isRanked();
        this.playersInQueue = new CopyOnWriteArrayList<>();
    }

    public void addToQueue(Player player, PracticePlayer practicePlayer) {
        playersInQueue.add(practicePlayer);
        practicePlayer.setState(PracticePlayer.State.QUEUE);
        PlayerUtil.resetPlayer(player);
        ItemUtil.setQueueLoadout(player);
        Objects.requireNonNull(Bukkit.getPlayer(practicePlayer.getUuid())).sendMessage(Component.text("You have been added to " + kit.getName() + " queue!").color(MC.CC.GREEN.getTextColor()));
    }

    public void removeFromQueue(PracticePlayer practicePlayer) {
        playersInQueue.remove(practicePlayer);
    }

    public void doNextFight() {
        if (playersInQueue.size() < 2) {
            return;
        }
        PracticePlayer playerA = playersInQueue.get(0);
        PracticePlayer playerB = playersInQueue.get(1);
        CustomPractice.getManagerHandler().getDuelManager().startUnrankedDuel(playerA, playerB, new Request(kit.getName()));
        removeFromQueue(playerA);
        removeFromQueue(playerB);
    }

}
