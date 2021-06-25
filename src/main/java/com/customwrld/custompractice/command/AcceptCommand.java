package com.customwrld.custompractice.command;

import com.customwrld.customlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.player.PracticePlayer;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class AcceptCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("accept")
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((sender, args) -> {
                    PracticePlayer practicePlayer = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(sender);
                    if (practicePlayer == null) {
                        sender.sendMessage(Component.text("There was an error getting your profile!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    Player target = (Player) args[0];
                    if (target == null) {
                        sender.sendMessage(Component.text("The player you tried to duel could not be found!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    PracticePlayer practiceTarget = CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(target);
                    if (practiceTarget == null) {
                        sender.sendMessage(Component.text("There was an error getting the target's profile!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (practiceTarget.getState() != PracticePlayer.State.LOBBY) {
                        sender.sendMessage(Component.text("The player you tried to duel is not in the lobby!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (practicePlayer.getState() != PracticePlayer.State.LOBBY) {
                        sender.sendMessage(Component.text("You must be in the lobby to accept a duel!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    if (!practicePlayer.getActiveRequests().containsKey(target.getUniqueId())) {
                        sender.sendMessage(Component.text("There are no active duel requests to this player!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    CustomPractice.getManagerHandler().getDuelManager().startUnrankedDuel(practiceTarget, practicePlayer, practicePlayer.getActiveRequests().get(target.getUniqueId()));
                });
    }
}
