package com.customwrld.practice.duel;

import lombok.Getter;

@Getter
public class Request {

    private final String kit;
    private final long timestamp;

    public Request(String kit) {
        this.kit = kit;
        this.timestamp = System.currentTimeMillis();
    }
}
