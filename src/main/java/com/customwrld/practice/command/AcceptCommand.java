package com.customwrld.practice.command;

import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class AcceptCommand extends Command {

    private final ManagerHandler handler;

    public AcceptCommand() {
        this.handler = Practice.getPractice().getManagerHandler();
        this.command = "accept";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((sender, args) -> {
                    PracticePlayer practicePlayer = this.handler.getPracticePlayerManager().getPracticePlayer(sender);
                    if (practicePlayer == null) {
                        sender.sendMessage(Component.text("There was an error getting your profile!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    Player target = (Player) args[0];
                    if (target == null) {
                        sender.sendMessage(Component.text("The player you tried to duel could not be found!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    PracticePlayer practiceTarget = this.handler.getPracticePlayerManager().getPracticePlayer(target);
                    if (practiceTarget == null) {
                        sender.sendMessage(Component.text("There was an error getting the target's profile!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (practiceTarget.getState() != PracticePlayer.State.LOBBY) {
                        sender.sendMessage(Component.text("The player you tried to duel is not in the lobby!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (practicePlayer.getState() != PracticePlayer.State.LOBBY) {
                        sender.sendMessage(Component.text("You must be in the lobby to accept a duel!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (!practicePlayer.getActiveRequests().containsKey(target.getUniqueId())) {
                        sender.sendMessage(Component.text("There are no active duel requests to this player!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    this.handler.getDuelManager().startUnrankedDuel(practiceTarget, practicePlayer, practicePlayer.getActiveRequests().get(target.getUniqueId()));
                });
    }

}
