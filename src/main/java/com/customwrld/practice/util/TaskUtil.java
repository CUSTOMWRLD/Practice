package com.customwrld.practice.util;

import com.customwrld.practice.Practice;
import com.customwrld.practice.queue.Queue;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {

    public static BukkitRunnable queueTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Queue queue : Practice.getPractice().getManagerHandler().getQueueManager().getUnRankedQueue().values()) {
                queue.doNextFight();
            }
            for (Queue queue : Practice.getPractice().getManagerHandler().getQueueManager().getRankedQueue().values()) {
                queue.doNextFight();
            }
        }
    };
}
