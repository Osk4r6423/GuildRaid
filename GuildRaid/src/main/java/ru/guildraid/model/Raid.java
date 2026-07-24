package ru.guildraid.model;

import org.bukkit.Location;

import java.util.*;

public class Raid {
    public enum Phase { REGISTERING, ACTIVE, ENDED }

    private final String attackerGuildId;
    private final String defenderGuildId;
    private Phase phase = Phase.REGISTERING;
    private final Set<UUID> raiders = new HashSet<>();
    private final Set<UUID> defenders = new HashSet<>();
    private final Set<UUID> eliminatedRaiders = new HashSet<>();
    private final Map<String, Integer> crystalHits = new LinkedHashMap<>();
    private int crystalsBroken = 0;
    private long phaseEndAt;
    private boolean attackerVictory;

    public Raid(String attackerGuildId, String defenderGuildId) {
        this.attackerGuildId = attackerGuildId;
        this.defenderGuildId = defenderGuildId;
    }

    public static String key(Location l) {
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public String getAttackerGuildId() { return attackerGuildId; }
    public String getDefenderGuildId() { return defenderGuildId; }
    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }
    public Set<UUID> getRaiders() { return raiders; }
    public Set<UUID> getDefenders() { return defenders; }
    public Set<UUID> getEliminatedRaiders() { return eliminatedRaiders; }
    public Map<String, Integer> getCrystalHits() { return crystalHits; }
    public int getCrystalsBroken() { return crystalsBroken; }
    public void setCrystalsBroken(int crystalsBroken) { this.crystalsBroken = crystalsBroken; }
    public long getPhaseEndAt() { return phaseEndAt; }
    public void setPhaseEndAt(long phaseEndAt) { this.phaseEndAt = phaseEndAt; }
    public boolean isAttackerVictory() { return attackerVictory; }
    public void setAttackerVictory(boolean attackerVictory) { this.attackerVictory = attackerVictory; }

    public int maxRaiders() {
        int d = Math.max(defenders.size(), 1);
        return (int) Math.ceil(d * 1.5);
    }

    public boolean isParticipant(UUID u) {
        return raiders.contains(u) || defenders.contains(u);
    }
}