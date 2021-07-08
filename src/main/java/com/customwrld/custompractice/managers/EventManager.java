package com.customwrld.custompractice.managers;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.event.Event;
import com.customwrld.custompractice.event.impl.FFAEvent;
import com.customwrld.custompractice.player.PracticePlayer;
import com.customwrld.custompractice.queue.Queue;
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
        this.eventCollection = CustomPractice.getDatabase().getCollection("event");
        this.eventQueue = new HashMap<>();
        Arrays.asList(
                new FFAEvent()
        ).forEach((event) -> {
            events.put(event.getName(), event);
            eventQueue.put(event.getName(), new Queue(event));
        });
    }

    public void host(PracticePlayer practicePlayer, String event) {
        if (CustomPractice.getCustomPractice().getActiveEvent() != null) {
            return;
        }
        Event event1 = events.get(event);
        event1.setHost(practicePlayer);
        CustomPractice.getCustomPractice().setActiveEvent(event1);
    }
}
