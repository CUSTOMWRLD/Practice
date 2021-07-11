package com.customwrld.practice.command;

import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;

public class EventCommand extends Command {

    public EventCommand() {
        this.command = "event";
        this.permission = "practice.admin";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withPermission(this.permission);
    }

    // TODO: Event Command

}
