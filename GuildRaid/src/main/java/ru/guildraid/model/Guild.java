package ru.guildraid.model;

import java.util.*;

public class Guild {
    private final String id;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private double treasury;
    private int guildCoins;
    private int raidWins, raidLosses;
    private int defendWins, defendLosses;
    private int tributeCount;
    private int chestKeys;
    private long lifeSuppressedUntil;
    private Double ransomPrice;

    public Guild(String id) {
        this.id = id.toLowerCase(Locale.ROOT);
    }

    public String getId() { return id; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Set<UUID> getMembers() { return members; }
    public boolean isMember(UUID u) { return members.contains(u); }
    public boolean isLeader(UUID u) { return leader != null && leader.equals(u); }

    public double getTreasury() { return treasury; }
    public void setTreasury(double treasury) { this.treasury = treasury; }
    public void addTreasury(double v) { this.treasury += v; }

    public int getGuildCoins() { return guildCoins; }
    public void setGuildCoins(int guildCoins) { this.guildCoins = guildCoins; }

    public int getRaidWins() { return raidWins; }
    public void setRaidWins(int v) { raidWins = v; }
    public int getRaidLosses() { return raidLosses; }
    public void setRaidLosses(int v) { raidLosses = v; }
    public int getDefendWins() { return defendWins; }
    public void setDefendWins(int v) { defendWins = v; }
    public int getDefendLosses() { return defendLosses; }
    public void setDefendLosses(int v) { defendLosses = v; }
    public int getTributeCount() { return tributeCount; }
    public void setTributeCount(int v) { tributeCount = v; }
    public void incTribute() { tributeCount++; }

    public int getChestKeys() { return chestKeys; }
    public void setChestKeys(int chestKeys) { this.chestKeys = chestKeys; }
    public void addChestKeys(int n) { this.chestKeys += n; }

    public long getLifeSuppressedUntil() { return lifeSuppressedUntil; }
    public void setLifeSuppressedUntil(long lifeSuppressedUntil) { this.lifeSuppressedUntil = lifeSuppressedUntil; }
    public boolean isLifeSuppressed() { return System.currentTimeMillis() < lifeSuppressedUntil; }

    public Double getRansomPrice() { return ransomPrice; }
    public void setRansomPrice(Double ransomPrice) { this.ransomPrice = ransomPrice; }
}