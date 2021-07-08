package com.customwrld.practice.managers;

import com.customwrld.practice.Practice;
import com.customwrld.practice.kit.Kit;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.queue.Queue;
import com.customwrld.practice.util.TaskUtil;
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
        TaskUtil.queueTask.runTaskTimer(Practice.getPractice(), 20, 20);
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
