package com.customwrld.custompractice.managers;

import lombok.Getter;

public class ManagerHandler {

    @Getter private final ArenaManager arenaManager;

    @Getter private final PracticePlayerManager practicePlayerManager;

    @Getter private final DuelManager duelManager;

    @Getter private final KitManager kitManager;

    @Getter private final QueueManager queueManager;

    @Getter private final EventManager eventManager;

    public ManagerHandler() {
        this.arenaManager = new ArenaManager(this);
        this.practicePlayerManager = new PracticePlayerManager(this);
        this.duelManager = new DuelManager(this);
        this.kitManager = new KitManager(this);
        this.queueManager = new QueueManager(this);
        this.eventManager = new EventManager(this);
    }
}
