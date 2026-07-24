package ru.guildraid.raid;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.config.ConfigManager;
import ru.guildraid.model.Guild;
import ru.guildraid.model.Raid;

import java.util.*;

public class RaidManager {
    private final GuildRaidPlugin plugin;
    private final List<Raid> active = new ArrayList<>();
    private BukkitTask ticker;

    public RaidManager(GuildRaidPlugin plugin) {
        this.plugin = plugin;
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void shutdown() {
        if (ticker != null) ticker.cancel();
        active.clear();
    }

    public Optional<Raid> findByGuild(String guildId) {
        String id = guildId.toLowerCase(Locale.ROOT);
        return active.stream()
                .filter(r -> r.getPhase() != Raid.Phase.ENDED)
                .filter(r -> r.getAttackerGuildId().equals(id) || r.getDefenderGuildId().equals(id))
                .findFirst();
    }

    public Optional<Raid> findByPlayer(UUID uuid) {
        return active.stream()
                .filter(r -> r.getPhase() != Raid.Phase.ENDED)
                .filter(r -> r.isParticipant(uuid) || r.getEliminatedRaiders().contains(uuid))
                .findFirst();
    }

    public boolean startRaid(Player leader, String targetId) {
        Guild atk = plugin.guilds().getByPlayer(leader.getUniqueId());
        if (atk == null) {
            plugin.msg().send(leader, "not-in-guild");
            return false;
        }
        if (!atk.isLeader(leader.getUniqueId())) {
            plugin.msg().send(leader, "only-leader");
            return false;
        }
        Guild def = plugin.storage().getGuild(targetId);
        if (def == null) {
            plugin.msg().send(leader, "guild-not-found");
            return false;
        }
        if (atk.getId().equals(def.getId())) {
            plugin.msg().send(leader, "raid-self");
            return false;
        }
        if (findByGuild(atk.getId()).isPresent() || findByGuild(def.getId()).isPresent()) {
            plugin.msg().send(leader, "raid-already");
            return false;
        }

        Raid raid = new Raid(atk.getId(), def.getId());
        raid.getRaiders().add(leader.getUniqueId());
        raid.setPhaseEndAt(System.currentTimeMillis() + plugin.configs().regMinutes() * 60_000L);
        active.add(raid);

        Bukkit.broadcastMessage(plugin.msg().format("raid-started", Map.of(
                "target", plugin.guilds().display(def).replaceAll("§.", ""),
                "time", String.valueOf(plugin.configs().regMinutes())
        )));
        return true;
    }

    public void joinRaider(Player p) {
        Guild g = plugin.guilds().getByPlayer(p.getUniqueId());
        if (g == null) {
            plugin.msg().send(p, "not-in-guild");
            return;
        }
        Optional<Raid> opt = findByGuild(g.getId());
        if (opt.isEmpty() || opt.get().getPhase() != Raid.Phase.REGISTERING) {
            plugin.msg().send(p, "raid-not-registering");
            return;
        }
        Raid raid = opt.get();
        if (!raid.getAttackerGuildId().equals(g.getId())) {
            plugin.msg().send(p, "raid-not-registering");
            return;
        }
        if (raid.getRaiders().size() >= raid.maxRaiders()) {
            plugin.msg().send(p, "raid-full-raiders");
            return;
        }
        raid.getRaiders().add(p.getUniqueId());
        plugin.msg().send(p, "raid-join-raider");
    }

    public void joinDefender(Player p) {
        Guild g = plugin.guilds().getByPlayer(p.getUniqueId());
        if (g == null) {
            plugin.msg().send(p, "not-in-guild");
            return;
        }
        Optional<Raid> opt = findByGuild(g.getId());
        if (opt.isEmpty() || opt.get().getPhase() != Raid.Phase.REGISTERING) {
            plugin.msg().send(p, "raid-not-registering");
            return;
        }
        Raid raid = opt.get();
        if (!raid.getDefenderGuildId().equals(g.getId())) {
            plugin.msg().send(p, "raid-not-registering");
            return;
        }
        raid.getDefenders().add(p.getUniqueId());
        plugin.msg().send(p, "raid-join-defender");
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Iterator<Raid> it = active.iterator();
        while (it.hasNext()) {
            Raid raid = it.next();
            if (raid.getPhase() == Raid.Phase.ENDED) {
                it.remove();
                continue;
            }
            if (now < raid.getPhaseEndAt()) continue;

            if (raid.getPhase() == Raid.Phase.REGISTERING) {
                finishRegistration(raid);
            } else if (raid.getPhase() == Raid.Phase.ACTIVE) {
                endRaid(raid, false);
            }
        }
    }

    private void finishRegistration(Raid raid) {
        int minDef = plugin.configs().minDefenders();
        if (raid.getDefenders().size() < minDef || raid.getRaiders().isEmpty()) {
            Bukkit.broadcastMessage(plugin.msg().format("raid-cancelled-min",
                    Map.of("min", String.valueOf(minDef))));
            raid.setPhase(Raid.Phase.ENDED);
            return;
        }
        int maxR = (int) Math.ceil(raid.getDefenders().size() * 1.5);
        while (raid.getRaiders().size() > maxR) {
            UUID last = raid.getRaiders().stream().reduce((a, b) -> b).orElse(null);
            if (last != null) raid.getRaiders().remove(last);
        }

        ConfigManager.GuildDef def = plugin.configs().guild(raid.getDefenderGuildId());
        Material mat = plugin.configs().crystalMaterial();
        if (mat == null) mat = Material.RESPAWN_ANCHOR;
        int needHits = plugin.configs().crystalHits();
        if (def != null) {
            int limit = Math.min(def.crystals.size(), plugin.configs().crystalsToWin());
            for (int i = 0; i < limit; i++) {
                Location l = def.crystals.get(i).getBlock().getLocation();
                l.getBlock().setType(mat);
                raid.getCrystalHits().put(Raid.key(l), needHits);
            }
        }

        raid.setPhase(Raid.Phase.ACTIVE);
        raid.setPhaseEndAt(System.currentTimeMillis() + plugin.configs().raidMinutes() * 60_000L);
        Bukkit.broadcastMessage(plugin.msg().format("raid-begin", Map.of(
                "crystals", String.valueOf(plugin.configs().crystalsToWin()),
                "time", String.valueOf(plugin.configs().raidMinutes())
        )));
    }

    public void onCrystalBroken(Raid raid, Player breaker) {
        raid.setCrystalsBroken(raid.getCrystalsBroken() + 1);
        int left = plugin.configs().crystalsToWin() - raid.getCrystalsBroken();
        Bukkit.broadcastMessage(plugin.msg().format("raid-crystal-broke",
                Map.of("left", String.valueOf(Math.max(0, left)))));

        for (String cmd : plugin.configs().crystalCommands()) {
            String c = cmd
                    .replace("%player%", breaker.getName())
                    .replace("%guild%", raid.getDefenderGuildId())
                    .replace("%crystal%", String.valueOf(raid.getCrystalsBroken()));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
        }

        if (raid.getCrystalsBroken() >= plugin.configs().crystalsToWin()) {
            endRaid(raid, true);
        }
    }

    private void endRaid(Raid raid, boolean attackersWin) {
        if (raid.getPhase() == Raid.Phase.ENDED) return;
        raid.setPhase(Raid.Phase.ENDED);
        raid.setAttackerVictory(attackersWin);

        Guild atk = plugin.storage().getGuild(raid.getAttackerGuildId());
        Guild def = plugin.storage().getGuild(raid.getDefenderGuildId());

        if (attackersWin) {
            Bukkit.broadcastMessage(plugin.msg().get("raid-win-attack"));
            if (atk != null) {
                atk.setRaidWins(atk.getRaidWins() + 1);
                atk.addChestKeys(2);
            }
            if (def != null) def.setDefendLosses(def.getDefendLosses() + 1);
            rewardCoins(raid.getRaiders(), 20);
            notifyLeadersChoice(raid);
        } else {
            Bukkit.broadcastMessage(plugin.msg().get("raid-win-defend"));
            if (def != null) {
                def.setDefendWins(def.getDefendWins() + 1);
                def.addChestKeys(2);
            }
            if (atk != null) atk.setRaidLosses(atk.getRaidLosses() + 1);
            rewardCoins(raid.getDefenders(), 20);
        }
        Bukkit.broadcastMessage(plugin.msg().get("raid-keys"));
    }

    private void rewardCoins(Set<UUID> uuids, int amount) {
        for (UUID u : uuids) {
            plugin.storage().getPlayer(u).addCoins(amount);
        }
    }

    private void notifyLeadersChoice(Raid raid) {
        Guild atk = plugin.storage().getGuild(raid.getAttackerGuildId());
        if (atk == null || atk.getLeader() == null) return;
        Player leader = Bukkit.getPlayer(atk.getLeader());
        if (leader != null) {
            leader.sendMessage("Победа! Выберите: /guild raid suppress  или  /guild raid ransom <сумма>");
        }
    }

    public void suppressLife(Player leader) {
        Guild atk = plugin.guilds().getByPlayer(leader.getUniqueId());
        if (atk == null || !atk.isLeader(leader.getUniqueId())) {
            plugin.msg().send(leader, "only-leader");
            return;
        }
        Optional<Raid> opt = active.stream()
                .filter(r -> r.getAttackerGuildId().equals(atk.getId()) && r.isAttackerVictory())
                .findFirst();
        if (opt.isEmpty()) {
            plugin.msg().send(leader, "no-raid");
            return;
        }
        Raid raid = opt.get();
        Guild def = plugin.storage().getGuild(raid.getDefenderGuildId());
        if (def == null) return;
        long until = System.currentTimeMillis() + plugin.configs().lifeHours() * 3_600_000L;
        def.setLifeSuppressedUntil(until);
        Bukkit.broadcastMessage(plugin.msg().format("raid-suppress", Map.of(
                "guild", def.getId(),
                "hours", String.valueOf(plugin.configs().lifeHours())
        )));
    }

    public void setRansom(Player leader, double price) {
        Guild atk = plugin.guilds().getByPlayer(leader.getUniqueId());
        if (atk == null || !atk.isLeader(leader.getUniqueId())) {
            plugin.msg().send(leader, "only-leader");
            return;
        }
        Optional<Raid> opt = active.stream()
                .filter(r -> r.getAttackerGuildId().equals(atk.getId()) && r.isAttackerVictory())
                .findFirst();
        if (opt.isEmpty()) {
            plugin.msg().send(leader, "no-raid");
            return;
        }
        Guild def = plugin.storage().getGuild(opt.get().getDefenderGuildId());
        if (def == null) return;
        def.setRansomPrice(price);
        plugin.msg().send(leader, "raid-ransom-set", Map.of("amount", String.valueOf(price)));
    }

    public List<Raid> getActive() { return active; }
}