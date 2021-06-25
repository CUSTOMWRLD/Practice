package com.customwrld.custompractice.util;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.queue.Queue;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {

    public static BukkitRunnable queueTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Queue queue : CustomPractice.getManagerHandler().getQueueManager().getUnRankedQueue().values()) {
                queue.doNextFight();
            }
            for (Queue queue : CustomPractice.getManagerHandler().getQueueManager().getRankedQueue().values()) {
                queue.doNextFight();
            }
        }
    };
}
