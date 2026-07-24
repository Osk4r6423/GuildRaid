package ru.guildraid.config;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.guildraid.GuildRaidPlugin;

import java.io.File;
import java.util.Map;

public class Messages {
    private final GuildRaidPlugin plugin;
    private FileConfiguration cfg;

    public Messages(GuildRaidPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "messages.yml");
        cfg = YamlConfiguration.loadConfiguration(f);
    }

    public String get(String key) {
        String p = color(cfg.getString("prefix", ""));
        return p + color(cfg.getString(key, key));
    }

    public String format(String key, Map<String, String> ph) {
        String s = get(key);
        if (ph != null) {
            for (Map.Entry<String, String> e : ph.entrySet()) {
                s = s.replace("%" + e.getKey() + "%", e.getValue());
            }
        }
        return s;
    }

    public void send(CommandSender to, String key) {
        to.sendMessage(get(key));
    }

    public void send(CommandSender to, String key, Map<String, String> ph) {
        to.sendMessage(format(key, ph));
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}