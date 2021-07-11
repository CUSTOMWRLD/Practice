package com.customwrld.practice.gui;

import com.customwrld.commonlib.util.MC;
import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Request;
import com.customwrld.practice.managers.ManagerHandler;
import com.customwrld.practice.player.PracticePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

public class HostGui implements InventoryProvider {

    private final SmartInventory inventory;

    private PracticePlayer practicePlayer;

    private final ManagerHandler handler;

    public HostGui(Player player, PracticePlayer practicePlayer) {
        this.practicePlayer = practicePlayer;

        this.inventory = SmartInventory.builder()
                .provider(this)
                .size(1, 9)
                .title("Host an event.")
                .build();

        this.handler = Practice.getPractice().getManagerHandler();

        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (practicePlayer == null) {
            this.inventory.close(player);
            return;
        }

        if (this.handler.getEventManager().getEvents().isEmpty()) {
            contents.fill(ClickableItem.empty(new ItemFactory(Material.BARRIER).setName(Component.text("There are no events available").color(MC.CC.RED.getTextColor())).build()));
            return;
        }

        this.handler.getEventManager().getEvents().forEach((s, event) -> {
            ClickableItem item = ClickableItem.of(new ItemFactory(event.getIcon().getType())
                    .setName(Component.text(s, MC.CC.BRAND.getTextColor(), TextDecoration.BOLD))
                    .setLore(Component.empty())
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .build(), clickEvent -> {
                if (!player.hasPermission("practice.host")) {
                    player.sendMessage(Component.text("You don't have permission to host events.").color(MC.CC.RED.getTextColor()));
                    this.inventory.close(player);
                    return;
                }
                if (Practice.getPractice().getActiveEvent() != null) {
                    player.sendMessage(Component.text("There is currently an active event, wait for it to end.").color(MC.CC.RED.getTextColor()));
                    this.inventory.close(player);
                    return;
                }
                handler.getEventManager().host(this.practicePlayer, s);
                this.inventory.close(player);
            });
            contents.add(item);
        });
    }
}
