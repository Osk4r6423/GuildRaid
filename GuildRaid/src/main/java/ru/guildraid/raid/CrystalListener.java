package ru.guildraid.raid;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.model.Raid;

import java.util.Map;
import java.util.Optional;

public class CrystalListener implements Listener {
    private final GuildRaidPlugin plugin;

    public CrystalListener(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Optional<Raid> opt = plugin.raids().findByPlayer(p.getUniqueId());
        if (opt.isEmpty()) return;
        Raid raid = opt.get();
        if (raid.getPhase() != Raid.Phase.ACTIVE) return;

        String key = Raid.key(e.getBlock().getLocation());
        if (!raid.getCrystalHits().containsKey(key)) return;

        if (!raid.getRaiders().contains(p.getUniqueId()) || raid.getEliminatedRaiders().contains(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true);
        int left = raid.getCrystalHits().get(key) - 1;
        int max = plugin.configs().crystalHits();
        if (left > 0) {
            raid.getCrystalHits().put(key, left);
            plugin.msg().send(p, "raid-crystal-hit", Map.of(
                    "hits", String.valueOf(max - left),
                    "max", String.valueOf(max)
            ));
        } else {
            raid.getCrystalHits().remove(key);
            e.getBlock().setType(org.bukkit.Material.AIR);
            plugin.raids().onCrystalBroken(raid, p);
        }
    }
}