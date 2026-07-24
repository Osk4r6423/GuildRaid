package ru.guildraid.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.Messages;
import ru.guildraid.model.Guild;
import ru.guildraid.util.ItemBuilder;

public class TreasuryMenu {
    public static void open(GuildRaidPlugin plugin, Player player) {
        Guild g = plugin.guilds().getByPlayer(player.getUniqueId());
        if (g == null) return;
        GuiHolder holder = new GuiHolder(GuiHolder.Type.TREASURY);
        Inventory inv = Bukkit.createInventory(holder, 27, Messages.color("&8Казна: " + g.getTreasury()));
        holder.setInventory(inv);

        inv.setItem(11, new ItemBuilder(Material.LIME_CONCRETE).name("&aВнести 100").build());
        inv.setItem(12, new ItemBuilder(Material.LIME_CONCRETE).name("&aВнести 1000").build());
        inv.setItem(14, new ItemBuilder(Material.RED_CONCRETE).name("&cСнять 100").lore("&7Только лидер").build());
        inv.setItem(15, new ItemBuilder(Material.RED_CONCRETE).name("&cСнять 1000").lore("&7Только лидер").build());
        inv.setItem(22, new ItemBuilder(Material.ARROW).name("&7Назад").build());
        player.openInventory(inv);
    }
}