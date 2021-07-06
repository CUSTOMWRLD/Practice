package com.customwrld.custompractice.command;

import com.customwrld.commonlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Request;
import com.customwrld.custompractice.gui.UnrankedGui;
import com.customwrld.custompractice.player.PracticePlayer;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DuelCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("duel")
                .withAliases("1v1")
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
                    if (target == sender) {
                        sender.sendMessage(Component.text("You can't duel yourself!").color(MC.CC.RED.getTextColor()));
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
                    if (practiceTarget.getActiveRequests().containsKey(sender.getUniqueId())) {
                        sender.sendMessage(Component.text("You've already sent this player a duel, please wait before sending another one!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    new UnrankedGui(sender, practicePlayer, practiceTarget);
                    //CustomPractice.getManagerHandler().getPracticePlayerManager().sendDuelRequest(practicePlayer, practiceTarget, target);
                    //sender.sendMessage(Component.text("Sent a duel to " + target.getName() + "!").color(MC.CC.GREEN.getTextColor()));
                });
    }

}
