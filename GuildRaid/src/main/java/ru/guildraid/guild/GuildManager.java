package ru.guildraid.guild;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.ConfigManager;
import ru.guildraid.model.Guild;
import ru.guildraid.model.GuildPlayer;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
    private final GuildRaidPlugin plugin;

    public GuildManager(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    public Guild getByPlayer(UUID uuid) {
        GuildPlayer gp = plugin.storage().getPlayer(uuid);
        if (gp.getGuildId() == null) return null;
        return plugin.storage().getGuild(gp.getGuildId());
    }

    public void joinGuild(Player player, String guildId) {
        Guild g = plugin.storage().getGuild(guildId);
        if (g == null) return;
        GuildPlayer gp = plugin.storage().getPlayer(player.getUniqueId());
        if (gp.getGuildId() != null) {
            Guild old = plugin.storage().getGuild(gp.getGuildId());
            if (old != null) old.getMembers().remove(player.getUniqueId());
        }
        gp.setGuildId(g.getId());
        g.getMembers().add(player.getUniqueId());
        if (g.getLeader() == null) g.setLeader(player.getUniqueId());
    }

    public boolean warp(Player player) {
        Guild g = getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return false;
        }
        ConfigManager.GuildDef def = plugin.configs().guild(g.getId());
        if (def == null || def.warp == null) {
            plugin.msg().send(player, "warp-no-location");
            return false;
        }
        player.teleport(def.warp);
        plugin.msg().send(player, "warp-success");
        return true;
    }

    public void claimTribute(Player player) {
        Guild g = getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return;
        }
        GuildPlayer gp = plugin.storage().getPlayer(player.getUniqueId());
        long today = LocalDate.now().toEpochDay();
        if (gp.getLastTributeDay() == today) {
            plugin.msg().send(player, "tribute-already");
            return;
        }
        int amount = plugin.configs().dailyTribute();
        gp.setLastTributeDay(today);
        gp.addCoins(amount);
        g.incTribute();
        plugin.msg().send(player, "tribute-received", Map.of("amount", String.valueOf(amount)));
    }

    public void upgradeApples(Player player) {
        Guild g = getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return;
        }
        GuildPlayer gp = plugin.storage().getPlayer(player.getUniqueId());
        int max = plugin.configs().maxApples();
        if (gp.getGoldenApples() >= max) {
            plugin.msg().send(player, "upgrade-max");
            return;
        }
        int cost = 5;
        if (!gp.takeCoins(cost)) {
            plugin.msg().send(player, "upgrade-no-coins");
            return;
        }
        gp.setGoldenApples(gp.getGoldenApples() + 1);
        plugin.msg().send(player, "upgrade-apples", Map.of(
                "amount", String.valueOf(gp.getGoldenApples()),
                "max", String.valueOf(max)
        ));
    }

    public boolean depositTreasury(Player player, double amount) {
        if (amount <= 0) return false;
        Guild g = getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return false;
        }
        if (!plugin.vault().withdraw(player, amount)) {
            plugin.msg().send(player, "treasury-no-money");
            return false;
        }
        g.addTreasury(amount);
        plugin.msg().send(player, "treasury-deposit", Map.of("amount", String.valueOf(amount)));
        return true;
    }

    public boolean withdrawTreasury(Player player, double amount) {
        if (amount <= 0) return false;
        Guild g = getByPlayer(player.getUniqueId());
        if (g == null) {
            plugin.msg().send(player, "not-in-guild");
            return false;
        }
        if (!g.isLeader(player.getUniqueId())) {
            plugin.msg().send(player, "treasury-only-leader-withdraw");
            return false;
        }
        if (g.getTreasury() < amount) {
            plugin.msg().send(player, "treasury-no-funds");
            return false;
        }
        g.addTreasury(-amount);
        plugin.vault().deposit(player, amount);
        plugin.msg().send(player, "treasury-withdraw", Map.of("amount", String.valueOf(amount)));
        return true;
    }

    public void setWarp(String guildId, Location loc) {
        String path = "guilds." + guildId.toLowerCase() + ".warp";
        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", loc.getYaw());
        plugin.getConfig().set(path + ".pitch", loc.getPitch());
        plugin.saveConfig();
        plugin.configs().reload();
    }

    public String display(Guild g) {
        ConfigManager.GuildDef def = plugin.configs().guild(g.getId());
        if (def == null) return g.getId();
        return def.color + def.displayName;
    }

    public String leaderName(Guild g) {
        if (g.getLeader() == null) return "-";
        String name = Bukkit.getOfflinePlayer(g.getLeader()).getName();
        return name == null ? g.getLeader().toString() : name;
    }
}