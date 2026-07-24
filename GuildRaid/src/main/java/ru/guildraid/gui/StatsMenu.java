package ru.guildraid.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.Messages;
import ru.guildraid.model.Guild;
import ru.guildraid.util.ItemBuilder;

public class StatsMenu {
    public static void open(GuildRaidPlugin plugin, Player player) {
        Guild g = plugin.guilds().getByPlayer(player.getUniqueId());
        if (g == null) return;
        GuiHolder holder = new GuiHolder(GuiHolder.Type.STATS);
        Inventory inv = Bukkit.createInventory(holder, 27, Messages.color("&8Статистика"));
        holder.setInventory(inv);

        inv.setItem(13, new ItemBuilder(Material.PAPER)
                .name("&6" + g.getId())
                .lore(
                        "&7Лидер: &f" + plugin.guilds().leaderName(g),
                        "&7Участников: &f" + g.getMembers().size(),
                        "&7Казна: &e" + g.getTreasury(),
                        "&7Рейды: &a" + g.getRaidWins() + " &7/ &c" + g.getRaidLosses(),
                        "&7Защиты: &a" + g.getDefendWins() + " &7/ &c" + g.getDefendLosses(),
                        "&7Дань (счётчик): &f" + g.getTributeCount(),
                        "&7Ключи сундука: &f" + g.getChestKeys(),
                        g.isLifeSuppressed() ? "&cЖизнь погашена!" : "&aЖизнь клана активна"
                ).build());
        inv.setItem(22, new ItemBuilder(Material.ARROW).name("&7Назад").build());
        player.openInventory(inv);
    }
}