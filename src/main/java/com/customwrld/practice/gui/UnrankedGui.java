package com.customwrld.practice.gui;

import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.commonlib.util.MC;
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

import java.util.Objects;

public class UnrankedGui implements InventoryProvider {

    private final SmartInventory inventory;

    private final PracticePlayer practicePlayer;

    private final PracticePlayer target;

    private final ManagerHandler handler;

    public UnrankedGui(Player player, PracticePlayer practicePlayer, PracticePlayer target) {
        this.practicePlayer = practicePlayer;
        this.target = target;

        this.inventory = SmartInventory.builder()
                .provider(this)
                .size(1, 9)
                .title("Unranked Queue")
                .build();

        this.handler = Practice.getPractice().getManagerHandler();

        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PracticePlayer practicePlayer = this.handler.getPracticePlayerManager().getPracticePlayer(player);

        if (practicePlayer == null) {
            this.inventory.close(player);
            return;
        }

        // TODO: Maybe load it only once?
        if (this.handler.getQueueManager().getUnRankedQueue().isEmpty()) {
            this.handler.getQueueManager().loadQueues();

            if (this.handler.getQueueManager().getUnRankedQueue().isEmpty()) {
                contents.fill(ClickableItem.empty(new ItemFactory(Material.BARRIER).setName(Component.text("There are no queues available").color(MC.CC.RED.getTextColor())).build()));
                return;
            }
        }

        this.handler.getQueueManager().getUnRankedQueue().forEach((s, queue) -> {
            ClickableItem item = ClickableItem.of(new ItemFactory(queue.getKit().getIcon().getType())
                    .setName(Component.text(s, MC.CC.BRAND.getTextColor(), TextDecoration.BOLD))
                    .setLore(Component.text()
                            .append(Component.text("Playing: ", MC.CC.BRAND_LIGHT.getTextColor()))
                            .append(Component.text("TODO", MC.CC.WHITE.getTextColor())).build(),
                            Component.text()
                                    .append(Component.text("Queuing: ", MC.CC.BRAND_LIGHT.getTextColor()))
                                    .append(Component.text(queue.getPlayersInQueue().size(), MC.CC.WHITE.getTextColor())).build(),
                            Component.empty(),
                            Component.text("Click to play!", MC.CC.BRAND.getTextColor()))
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .build(), event -> {
                if (target == null) {
                    queue.addToQueue(player, practicePlayer);
                } else {
                    Request request = new Request(queue.getKit().getName());
                    this.handler.getPracticePlayerManager().sendDuelRequest(practicePlayer, target, Bukkit.getPlayer(target.getUuid()), request);
                    player.sendMessage(Component.text("Sent a duel to " + target.getName() + "!").color(MC.CC.GREEN.getTextColor()));
                }
                this.inventory.close(player);
            });
            contents.add(item);
        });
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        this.init(player, contents);
    }

}
