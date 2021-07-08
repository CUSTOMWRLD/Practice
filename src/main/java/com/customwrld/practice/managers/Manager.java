package com.customwrld.practice.managers;

public class Manager {
    protected ManagerHandler handler;

    public Manager(ManagerHandler handler) {
        this.handler = handler;
    }
}
