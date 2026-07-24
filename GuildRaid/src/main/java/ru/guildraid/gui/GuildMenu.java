package ru.guildraid.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.Messages;
import ru.guildraid.model.Guild;
import ru.guildraid.model.GuildPlayer;
import ru.guildraid.util.ItemBuilder;

public class GuildMenu {
    private final GuildRaidPlugin plugin;
    private final Player player;

    public GuildMenu(GuildRaidPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        Guild g = plugin.guilds().getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return;
        }
        if (g.isLifeSuppressed() && plugin.configs().suppressedFeatures().contains("guild-menus")
                && !g.isLeader(player.getUniqueId())) {
            player.sendMessage(Messages.color("&cМеню гильдии отключено (погашение жизни клана)."));
            return;
        }

        GuiHolder holder = new GuiHolder(GuiHolder.Type.MAIN);
        Inventory inv = Bukkit.createInventory(holder, 27, Messages.color("&8Меню гильдии"));
        holder.setInventory(inv);

        inv.setItem(10, new ItemBuilder(Material.GOLD_INGOT)
                .name("&6Казна")
                .lore("&7Деньги: &e" + g.getTreasury(), "&eЛКМ — открыть")
                .build());
        inv.setItem(12, new ItemBuilder(Material.IRON_SWORD)
                .name("&cРейд")
                .lore("&7Система рейдов")
                .build());
        inv.setItem(14, new ItemBuilder(Material.GOLDEN_APPLE)
                .name("&eПрокачка")
                .lore("&7Зол. яблоки на рейд", "&eЛКМ — улучшить (5 монет)")
                .build());
        inv.setItem(16, new ItemBuilder(Material.EMERALD)
                .name("&aДань")
                .lore("&7Ежедневные монеты гильдии")
                .build());
        inv.setItem(22, new ItemBuilder(Material.BOOK)
                .name("&bСтатистика")
                .lore("&7Информация о клане")
                .build());

        GuildPlayer gp = plugin.storage().getPlayer(player.getUniqueId());
        inv.setItem(4, new ItemBuilder(Material.PLAYER_HEAD)
                .name("&fВаши монеты: &e" + gp.getPersonalGuildCoins())
                .lore("&7Яблоки: &e" + gp.getGoldenApples())
                .build());

        player.openInventory(inv);
    }
}