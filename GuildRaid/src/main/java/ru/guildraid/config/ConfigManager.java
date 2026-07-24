package ru.guildraid.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import ru.guildraid.GuildRaidPlugin;

import java.util.*;

public class ConfigManager {

    private final GuildRaidPlugin plugin;
    private final Map<String, GuildDef> guildDefs = new LinkedHashMap<>();

    public ConfigManager(GuildRaidPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        guildDefs.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("guilds");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            GuildDef def = new GuildDef();
            def.id = id.toLowerCase(Locale.ROOT);
            def.displayName = s.getString("display-name", id);
            def.color = s.getString("color", "&f");
            def.warp = readLoc(s.getConfigurationSection("warp"));
            def.crystals = new ArrayList<>();
            List<Map<?, ?>> list = s.getMapList("crystals");
            for (Map<?, ?> m : list) {
                def.crystals.add(mapToLoc(m));
            }
            guildDefs.put(def.id, def);
        }
    }

    private Location readLoc(ConfigurationSection s) {
        if (s == null) return null;
        World w = Bukkit.getWorld(s.getString("world", "world"));
        if (w == null) return null;
        return new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
                (float) s.getDouble("yaw"), (float) s.getDouble("pitch"));
    }

    private Location mapToLoc(Map<?, ?> m) {
        World w = Bukkit.getWorld(String.valueOf(m.get("world")));
        if (w == null) w = Bukkit.getWorlds().get(0);
        int x = ((Number) m.get("x")).intValue();
        int y = ((Number) m.get("y")).intValue();
        int z = ((Number) m.get("z")).intValue();
        return new Location(w, x, y, z);
    }

    public Map<String, GuildDef> guildDefs() { return guildDefs; }
    public GuildDef guild(String id) { return guildDefs.get(id.toLowerCase(Locale.ROOT)); }

    public int raidMinutes() { return plugin.getConfig().getInt("settings.raid-duration-minutes", 30); }
    public int regMinutes() { return plugin.getConfig().getInt("settings.registration-duration-minutes", 10); }
    public int lifeHours() { return plugin.getConfig().getInt("settings.life-suppress-hours", 4); }
    public int crystalHits() { return plugin.getConfig().getInt("settings.crystal-hits", 50); }
    public int crystalsToWin() { return plugin.getConfig().getInt("settings.crystals-to-win", 10); }
    public int maxApples() { return plugin.getConfig().getInt("settings.max-golden-apples", 5); }
    public int dailyTribute() { return plugin.getConfig().getInt("settings.daily-tribute-coins", 10); }
    public int minDefenders() { return plugin.getConfig().getInt("settings.min-defenders", 2); }
    public Material crystalMaterial() {
        return Material.matchMaterial(plugin.getConfig().getString("settings.crystal-material", "RESPAWN_ANCHOR"));
    }
    public List<String> crystalCommands() {
        return plugin.getConfig().getStringList("settings.crystal-break-commands");
    }
    public List<String> suppressedFeatures() {
        return plugin.getConfig().getStringList("settings.suppressed-features");
    }

    public static class GuildDef {
        public String id;
        public String displayName;
        public String color;
        public Location warp;
        public List<Location> crystals;
    }
}