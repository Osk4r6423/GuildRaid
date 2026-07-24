package ru.guildraid.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.Messages;
import ru.guildraid.util.ItemBuilder;

public class RaidMenu {
    public static void open(GuildRaidPlugin plugin, Player player) {
        GuiHolder holder = new GuiHolder(GuiHolder.Type.RAID);
        Inventory inv = Bukkit.createInventory(holder, 27, Messages.color("&8Рейды"));
        holder.setInventory(inv);

        int slot = 10;
        for (String id : plugin.configs().guildDefs().keySet()) {
            var def = plugin.configs().guild(id);
            inv.setItem(slot++, new ItemBuilder(Material.TNT)
                    .name(def.color + "Напасть: " + def.displayName)
                    .lore("&7/guild raid start " + id, "&eЛКМ — старт (лидер)")
                    .build());
            if (slot > 16) break;
        }
        inv.setItem(20, new ItemBuilder(Material.IRON_SWORD).name("&cВступить в рейд").lore("&7join").build());
        inv.setItem(22, new ItemBuilder(Material.SHIELD).name("&aВступить в защиту").lore("&7defend").build());
        inv.setItem(24, new ItemBuilder(Material.ARROW).name("&7Назад").build());
        player.openInventory(inv);
    }
}