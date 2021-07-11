package com.customwrld.practice.command;

import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.gui.HostGui;
import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;

public class HostCommand extends Command {

    public HostCommand() {
        this.command = "host";
        this.permission = "practice.host";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withPermission(this.permission)
                .executesPlayer((sender, args) -> {
                    if (Practice.getPractice().getActiveEvent() != null) {
                        sender.sendMessage(Component.text("There is currently an active event, wait for it to end.").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    new HostGui(sender, Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(sender));
                });
    }

    // TODO: Host Command

}
