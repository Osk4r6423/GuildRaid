package ru.guildraid.model;

import java.util.UUID;

public class GuildPlayer {
    private final UUID uuid;
    private String guildId;
    private int goldenApples = 0;
    private long lastTributeDay = -1;
    private int personalGuildCoins = 0;

    public GuildPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }
    public int getGoldenApples() { return goldenApples; }
    public void setGoldenApples(int goldenApples) { this.goldenApples = goldenApples; }
    public long getLastTributeDay() { return lastTributeDay; }
    public void setLastTributeDay(long lastTributeDay) { this.lastTributeDay = lastTributeDay; }
    public int getPersonalGuildCoins() { return personalGuildCoins; }
    public void setPersonalGuildCoins(int personalGuildCoins) { this.personalGuildCoins = personalGuildCoins; }
    public void addCoins(int n) { personalGuildCoins += n; }
    public boolean takeCoins(int n) {
        if (personalGuildCoins < n) return false;
        personalGuildCoins -= n;
        return true;
    }
}