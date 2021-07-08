package com.customwrld.practice.gui;

import com.customwrld.customlib.smartinvs.ClickableItem;
import com.customwrld.customlib.smartinvs.SmartInventory;
import com.customwrld.customlib.smartinvs.content.InventoryContents;
import com.customwrld.customlib.smartinvs.content.InventoryProvider;
import com.customwrld.customlib.util.ItemFactory;
import com.customwrld.commonlib.util.MC;
import com.customwrld.practice.Practice;
import com.customwrld.practice.duel.Request;
import com.customwrld.practice.player.PracticePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;

public class UnrankedGui implements InventoryProvider {

    private final SmartInventory inventory;

    private final PracticePlayer practicePlayer;

    private final PracticePlayer target;

    public UnrankedGui(Player player, PracticePlayer practicePlayer, PracticePlayer target) {
        this.practicePlayer = practicePlayer;
        this.target = target;

        this.inventory = SmartInventory.builder()
                .id("unRankedGUI")
                .provider(this)
                .size(1, 9)
                .title("Join an unranked queue.").build();

        this.inventory.open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        PracticePlayer practicePlayer = Practice.getPractice().getManagerHandler().getPracticePlayerManager().getPracticePlayer(player);

        if (practicePlayer == null) {
            this.inventory.close(player);
            return;
        }

        if (Practice.getPractice().getManagerHandler().getQueueManager().getUnRankedQueue().isEmpty()) {
            Practice.getPractice().getManagerHandler().getQueueManager().loadQueues();
            if (Practice.getPractice().getManagerHandler().getQueueManager().getUnRankedQueue().isEmpty()) {
                contents.fill(ClickableItem.empty(new ItemFactory(Material.BARRIER).setName(Component.text("There are no queues available").color(MC.CC.RED.getTextColor())).build()));
                return;
            }
        }

        Practice.getPractice().getManagerHandler().getQueueManager().getUnRankedQueue().forEach(((s, queue) -> {
            ClickableItem item = ClickableItem.of(new ItemFactory(queue.getKit().getIcon().getType()).setName(Component.text(s)).build(), event -> {
                if (target == null) {
                    queue.addToQueue(player, practicePlayer);
                } else {
                    Request request = new Request(queue.getKit().getName());
                    Practice.getPractice().getManagerHandler().getPracticePlayerManager().sendDuelRequest(practicePlayer, target, Objects.requireNonNull(Bukkit.getPlayer(target.getUuid())), request);
                    player.sendMessage(Component.text("Sent a duel to " + target.getName() + "!").color(MC.CC.GREEN.getTextColor()));
                }
                this.inventory.close(player);
            });
            contents.add(item);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        InventoryProvider.super.update(player, contents);
    }
}
