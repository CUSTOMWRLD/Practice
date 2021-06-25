package com.customwrld.custompractice.duel;

import lombok.Getter;

public class Request {
    @Getter
    private final String kit;

    @Getter
    private final long timestamp;

    public Request(String kit) {
        this.kit = kit;
        this.timestamp = System.currentTimeMillis();
    }
}
