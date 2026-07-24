package ru.guildraid.raid;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.ConfigManager;
import ru.guildraid.model.Raid;

import java.util.Optional;

public class RaidListener implements Listener {
    private final GuildRaidPlugin plugin;

    public RaidListener(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Optional<Raid> opt = plugin.raids().findByPlayer(p.getUniqueId());
        if (opt.isEmpty()) return;
        Raid raid = opt.get();
        if (raid.getPhase() != Raid.Phase.ACTIVE) return;

        if (raid.getRaiders().contains(p.getUniqueId())) {
            raid.getEliminatedRaiders().add(p.getUniqueId());
            raid.getRaiders().remove(p.getUniqueId());
            plugin.msg().send(p, "raid-death-raider");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Optional<Raid> opt = plugin.raids().findByPlayer(p.getUniqueId());
        if (opt.isEmpty()) return;
        Raid raid = opt.get();
        if (raid.getPhase() != Raid.Phase.ACTIVE) return;
        if (!raid.getDefenders().contains(p.getUniqueId())) return;

        ConfigManager.GuildDef def = plugin.configs().guild(raid.getDefenderGuildId());
        if (def != null && def.warp != null) {
            e.setRespawnLocation(def.warp);
        }
    }
}