package com.customwrld.practice.command;

import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Duel;
import com.customwrld.practice.managers.DuelManager;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.managers.PracticePlayerManager;
import com.customwrld.practice.player.PracticePlayer;
import com.customwrld.practice.util.Command;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class SpectateCommand extends Command {

    private final ManagerHandler handler;

    public SpectateCommand() {
        this.handler = Practice.getPractice().getManagerHandler();
        this.command = "spectate";
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand(this.command)
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((sender, args) -> {
                    DuelManager duelManager = this.handler.getDuelManager();
                    PracticePlayerManager practicePlayerManager = this.handler.getPracticePlayerManager();
                    Player player = (Player) args[0];
                    Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));

                    if (duel == null) {
                        sender.sendMessage(Component.text("This player is not in a duel!", MC.CC.RED.getTextColor()));
                        return;
                    }

                    PracticePlayer practicePlayer = practicePlayerManager.getPracticePlayer(sender);
                    practicePlayer.setState(PracticePlayer.State.SPECTATING);
                    duel.addSpectator(sender.getName(), sender.getUniqueId(), sender, practicePlayer);
                    sender.teleport(player.getLocation());
                });
    }

}
