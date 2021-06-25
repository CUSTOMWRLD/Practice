package com.customwrld.custompractice.util;

import com.customwrld.custompractice.CustomPractice;
import com.customwrld.custompractice.managers.DuelManager;
import com.customwrld.custompractice.managers.PracticePlayerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    private final String author;
    private final String identifier;
    private final String version;

    public Placeholders(String author, String identifier, String version) {
        this.author = author;
        this.identifier = identifier;
        this.version = version;
    }
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }
    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        DuelManager duelManager = CustomPractice.getManagerHandler().getDuelManager();
        return switch (identifier) {
            case ("online") -> String.valueOf(Bukkit.getServer().getOnlinePlayers().size());
            case ("infights") -> String.valueOf(CustomPractice.getManagerHandler().getDuelManager().getPlayerToDuel().size());
            //case ("state") -> String.valueOf(CustomPractice.getManagerHandler().getPracticePlayerManager().getPracticePlayer(player).getState());
            //case ("fighting") -> String.valueOf(duelManager.getActiveDuels().get(duelManager.getPlayerToDuel().get(player.getUniqueId())).getOpponent(player.getUniqueId()));
            default -> null;
        };
    }
}
