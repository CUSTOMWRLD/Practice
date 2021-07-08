package com.customwrld.practice.command;

import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.gui.UnrankedGui;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DuelCommand extends Command {

    public DuelCommand() {
        this.command = "duel";
        this.aliases = new String[]{"1v1"};
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withAliases(this.aliases)
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((sender, args) -> {
                    PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(sender);

                    if (practicePlayer == null) {
                        sender.sendMessage(Component.text("There was an error getting your profile!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    Player target = (Player) args[0];

                    if (target == null) {
                        sender.sendMessage(Component.text("The player you tried to duel could not be found!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    if (target == sender) {
                        sender.sendMessage(Component.text("You can't duel yourself!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    PracticePlayer practiceTarget = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(target);

                    if (practiceTarget == null) {
                        sender.sendMessage(Component.text("There was an error getting the target's profile!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    if (practiceTarget.getState() != PracticePlayer.State.LOBBY) {
                        sender.sendMessage(Component.text("The player you tried to duel is not in the lobby!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    if (practiceTarget.getActiveRequests().containsKey(sender.getUniqueId())) {
                        sender.sendMessage(Component.text("You've already sent this player a duel, please wait before sending another one!", MC.CC.RED.getTextColor()));
                        return;
                    }
                    new UnrankedGui(sender, practicePlayer, practiceTarget);
                    //CustomPractice.getCustomPractice().getManagerHandler().getPracticePlayerManager().sendDuelRequest(practicePlayer, practiceTarget, target);
                    //sender.sendMessage(Component.text("Sent a duel to " + target.getName() + "!", MC.CC.GREEN.getTextColor()));
                });
    }

}
