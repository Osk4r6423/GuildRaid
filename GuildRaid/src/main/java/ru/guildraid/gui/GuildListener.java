package ru.guildraid.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.Messages;

public class GuiListener implements Listener {
    private final GuildRaidPlugin plugin;

    public GuiListener(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!(e.getInventory().getHolder() instanceof GuiHolder holder)) return;
        e.setCancelled(true);
        ItemStack cur = e.getCurrentItem();
        if (cur == null || !cur.hasItemMeta()) return;

        int slot = e.getRawSlot();

        switch (holder.getType()) {
            case MAIN -> handleMain(player, slot);
            case TREASURY -> handleTreasury(player, slot);
            case RAID -> handleRaid(player, slot, cur);
            case STATS -> {
                if (slot == 22) new GuildMenu(plugin, player).open();
            }
        }
    }

    private void handleMain(Player player, int slot) {
        switch (slot) {
            case 10 -> TreasuryMenu.open(plugin, player);
            case 12 -> RaidMenu.open(plugin, player);
            case 14 -> plugin.guilds().upgradeApples(player);
            case 16 -> plugin.guilds().claimTribute(player);
            case 22 -> StatsMenu.open(plugin, player);
        }
    }

    private void handleTreasury(Player player, int slot) {
        switch (slot) {
            case 11 -> plugin.guilds().depositTreasury(player, 100);
            case 12 -> plugin.guilds().depositTreasury(player, 1000);
            case 14 -> plugin.guilds().withdrawTreasury(player, 100);
            case 15 -> plugin.guilds().withdrawTreasury(player, 1000);
            case 22 -> new GuildMenu(plugin, player).open();
        }
        if (slot != 22) TreasuryMenu.open(plugin, player);
    }

    private void handleRaid(Player player, int slot, ItemStack cur) {
        if (slot == 20) {
            plugin.raids().joinRaider(player);
            return;
        }
        if (slot == 22) {
            plugin.raids().joinDefender(player);
            return;
        }
        if (slot == 24) {
            new GuildMenu(plugin, player).open();
            return;
        }
        if (cur.getItemMeta() != null && cur.getItemMeta().lore() != null) {
            for (var line : cur.getItemMeta().lore()) {
                String s = Messages.color(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line));
                if (s.contains("raid start")) {
                    String[] parts = s.trim().split(" ");
                    String id = parts[parts.length - 1];
                    plugin.raids().startRaid(player, id);
                    player.closeInventory();
                    return;
                }
            }
        }
    }
}