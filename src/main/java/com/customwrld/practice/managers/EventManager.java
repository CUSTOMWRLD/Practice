package com.customwrld.practice.managers;

import com.customwrld.practice.Practice;
import com.customwrld.practice.event.Event;
import com.customwrld.practice.event.impl.FFAEvent;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.queue.Queue;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EventManager extends Manager {

    @Getter private final Map<String, Event> events;

    @Getter private final Map<String, Queue> eventQueue;

    @Getter private final MongoCollection<Document> eventCollection;

    public EventManager(ManagerHandler handler) {
        super(handler);
        this.events = new HashMap<>();
        this.eventCollection = Practice.getPractice().getDatabase().getCollection("event");
        this.eventQueue = new HashMap<>();
        Arrays.asList(
                new FFAEvent()
        ).forEach((event) -> {
            events.put(event.getName(), event);
            eventQueue.put(event.getName(), new Queue(event));
        });
    }

    public void host(PracticePlayer practicePlayer, String event) {
        if (Practice.getPractice().getActiveEvent() != null) {
            return;
        }
        Event event1 = events.get(event);
        event1.setHost(practicePlayer);
        Practice.getPractice().setActiveEvent(event1);
    }
}
