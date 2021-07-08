package com.customwrld.practice.util;

import dev.jorel.commandapi.CommandAPICommand;

public abstract class Command {

    public String command;
    public String permission;
    public String[] aliases;

    public abstract CommandAPICommand command();

}
