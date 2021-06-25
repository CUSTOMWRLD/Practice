package com.customwrld.custompractice.event;

import com.customwrld.custompractice.player.PracticePlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Event {

    @Getter @Setter private State state;
    @Getter private Map<UUID, PracticePlayer> players;

    public Event() {
        state = State.INIT;
        players = new HashMap<>();
    }

    public enum State {
        INIT,
        WAITING,
        STARTED,
        ENDED
    }
}
