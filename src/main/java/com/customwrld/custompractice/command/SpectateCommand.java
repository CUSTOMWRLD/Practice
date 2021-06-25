package com.customwrld.custompractice.command;

import com.customwrld.customlib.util.MC;
import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.duel.Duel;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.PracticePlayerManager;
import com.customwrld.custompractice.player.PracticePlayer;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class SpectateCommand {
    public static CommandAPICommand command() {
        return new CommandAPICommand("spectate")
                .withArguments(new PlayerArgument("player"))
                .executesPlayer((sender, args) -> {
                    DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
                    PracticePlayerManager practicePlayerManager = CustomPractice.getManagerHandler().getPracticePlayerManager();
                    Player player = (Player) args[0];
                    Duel duel = duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId()));
                    if (duel == null) {
                        sender.sendMessage(Component.text("This player is not in a duel!").color(MC.CC.RED.getTextColor()));
                        return;
                    }
                    PracticePlayer practicePlayer = practicePlayerManager.getPracticePlayer(sender);
                    practicePlayer.setState(PracticePlayer.State.SPECTATING);
                    duel.addSpectator(sender.getName(), sender.getUniqueId(), sender, practicePlayer);
                    sender.teleport(player.getLocation());
                });
    }
}
