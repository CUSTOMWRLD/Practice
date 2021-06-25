package com.customwrld.custompractice.managers;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.kit.Kit;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.queue.Queue;
import com.customwrld.custompractice.util.TaskUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class QueueManager extends Manager {

    @Getter
    private final Map<String, Queue> unRankedQueue;

    @Getter
    private final Map<String, Queue> rankedQueue;

    public QueueManager(ManagerHandler handler) {
        super(handler);
        unRankedQueue = new HashMap<>();
        rankedQueue = new HashMap<>();
        loadQueues();
        TaskUtil.queueTask.runTaskTimer(CustomPractice.getCustomPractice(), 20, 20);
    }

    public void loadQueues() {
        for (Kit kit : handler.getKitManager().getKits().values()) {
            if (!kit.isRanked()) {
                unRankedQueue.put(kit.getName(), new Queue(kit));
            } else {
                rankedQueue.put(kit.getName(), new Queue(kit));
            }
        }
    }

    public Queue getQueue(PracticePlayer practicePlayer) {
        for (Queue queue : unRankedQueue.values()) {
            if (queue.getPlayersInQueue().contains(practicePlayer)) {
                return queue;
            }
        }
        for (Queue queue : rankedQueue.values()) {
            if (queue.getPlayersInQueue().contains(practicePlayer)) {
                return queue;
            }
        }
        return null;
    }
}
