package ru.guildraid.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.model.Guild;
import ru.guildraid.model.GuildPlayer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStorage {
    private final GuildRaidPlugin plugin;
    private final Map<String, Guild> guilds = new ConcurrentHashMap<>();
    private final Map<UUID, GuildPlayer> players = new ConcurrentHashMap<>();
    private File guildsFile;
    private File playersFile;

    public DataStorage(GuildRaidPlugin plugin) {
        this.plugin = plugin;
        guildsFile = new File(plugin.getDataFolder(), "data/guilds.yml");
        playersFile = new File(plugin.getDataFolder(), "data/players.yml");
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        new File(plugin.getDataFolder(), "data").mkdirs();

        plugin.configs().guildDefs().forEach((id, def) -> guilds.computeIfAbsent(id, Guild::new));

        if (guildsFile.exists()) {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(guildsFile);
            for (String id : y.getKeys(false)) {
                ConfigurationSection s = y.getConfigurationSection(id);
                if (s == null) continue;
                Guild g = guilds.computeIfAbsent(id.toLowerCase(Locale.ROOT), Guild::new);
                String leader = s.getString("leader");
                if (leader != null && !leader.isEmpty()) g.setLeader(UUID.fromString(leader));
                g.getMembers().clear();
                for (String m : s.getStringList("members")) g.getMembers().add(UUID.fromString(m));
                g.setTreasury(s.getDouble("treasury"));
                g.setGuildCoins(s.getInt("guild-coins"));
                g.setRaidWins(s.getInt("raid-wins"));
                g.setRaidLosses(s.getInt("raid-losses"));
                g.setDefendWins(s.getInt("defend-wins"));
                g.setDefendLosses(s.getInt("defend-losses"));
                g.setTributeCount(s.getInt("tribute-count"));
                g.setChestKeys(s.getInt("chest-keys"));
                g.setLifeSuppressedUntil(s.getLong("life-suppressed-until"));
                if (s.contains("ransom")) g.setRansomPrice(s.getDouble("ransom"));
            }
        }

        if (playersFile.exists()) {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(playersFile);
            for (String key : y.getKeys(false)) {
                ConfigurationSection s = y.getConfigurationSection(key);
                if (s == null) continue;
                UUID uuid = UUID.fromString(key);
                GuildPlayer p = new GuildPlayer(uuid);
                p.setGuildId(s.getString("guild"));
                p.setGoldenApples(s.getInt("golden-apples"));
                p.setLastTributeDay(s.getLong("last-tribute-day"));
                p.setPersonalGuildCoins(s.getInt("coins"));
                players.put(uuid, p);
            }
        }
    }

    public synchronized void save() {
        try {
            YamlConfiguration gy = new YamlConfiguration();
            for (Guild g : guilds.values()) {
                String path = g.getId();
                gy.set(path + ".leader", g.getLeader() == null ? "" : g.getLeader().toString());
                List<String> mem = new ArrayList<>();
                g.getMembers().forEach(u -> mem.add(u.toString()));
                gy.set(path + ".members", mem);
                gy.set(path + ".treasury", g.getTreasury());
                gy.set(path + ".guild-coins", g.getGuildCoins());
                gy.set(path + ".raid-wins", g.getRaidWins());
                gy.set(path + ".raid-losses", g.getRaidLosses());
                gy.set(path + ".defend-wins", g.getDefendWins());
                gy.set(path + ".defend-losses", g.getDefendLosses());
                gy.set(path + ".tribute-count", g.getTributeCount());
                gy.set(path + ".chest-keys", g.getChestKeys());
                gy.set(path + ".life-suppressed-until", g.getLifeSuppressedUntil());
                if (g.getRansomPrice() != null) gy.set(path + ".ransom", g.getRansomPrice());
            }
            gy.save(guildsFile);

            YamlConfiguration py = new YamlConfiguration();
            for (GuildPlayer p : players.values()) {
                String path = p.getUuid().toString();
                py.set(path + ".guild", p.getGuildId());
                py.set(path + ".golden-apples", p.getGoldenApples());
                py.set(path + ".last-tribute-day", p.getLastTributeDay());
                py.set(path + ".coins", p.getPersonalGuildCoins());
            }
            py.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения: " + e.getMessage());
        }
    }

    public Guild getGuild(String id) {
        if (id == null) return null;
        return guilds.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<Guild> allGuilds() { return guilds.values(); }

    public GuildPlayer getPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, GuildPlayer::new);
    }
}