package ru.guildraid;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.guildraid.command.GuildCommand;
import ru.guildraid.config.ConfigManager;
import ru.guildraid.config.Messages;
import ru.guildraid.economy.VaultHook;
import ru.guildraid.guild.GuildManager;
import ru.guildraid.gui.GuiListener;
import ru.guildraid.raid.CrystalListener;
import ru.guildraid.raid.RaidListener;
import ru.guildraid.raid.RaidManager;
import ru.guildraid.storage.DataStorage;

public final class GuildRaidPlugin extends JavaPlugin {

    private static GuildRaidPlugin instance;
    private ConfigManager configManager;
    private Messages messages;
    private DataStorage storage;
    private VaultHook vault;
    private GuildManager guildManager;
    private RaidManager raidManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        configManager = new ConfigManager(this);
        messages = new Messages(this);
        vault = new VaultHook(this);
        if (!vault.setup()) {
            getLogger().severe("Vault не найден! Отключаюсь...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        storage = new DataStorage(this);
        storage.load();

        guildManager = new GuildManager(this);
        raidManager = new RaidManager(this);

        GuildCommand cmd = new GuildCommand(this);
        getCommand("guild").setExecutor(cmd);
        getCommand("guild").setTabCompleter(cmd);

        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CrystalListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RaidListener(this), this);

        int mins = Math.max(1, getConfig().getInt("settings.auto-save-minutes", 5));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> storage.save(),
                20L * 60 * mins, 20L * 60 * mins);

        getLogger().info("GuildRaid включён.");
    }

    @Override
    public void onDisable() {
        if (raidManager != null) raidManager.shutdown();
        if (storage != null) storage.save();
    }

    public static GuildRaidPlugin get() { return instance; }
    public ConfigManager configs() { return configManager; }
    public Messages msg() { return messages; }
    public DataStorage storage() { return storage; }
    public VaultHook vault() { return vault; }
    public GuildManager guilds() { return guildManager; }
    public RaidManager raids() { return raidManager; }

    public void reloadAll() {
        reloadConfig();
        configManager.reload();
        messages.reload();
    }
}