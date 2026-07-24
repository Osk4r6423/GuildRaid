package ru.guildraid.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.guildraid.GuildRaidPlugin;

public class VaultHook {
    private final GuildRaidPlugin plugin;
    private Economy economy;

    public VaultHook(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean deposit(Player p, double amount) {
        return economy.depositPlayer(p, amount).transactionSuccess();
    }

    public boolean withdraw(Player p, double amount) {
        if (economy.getBalance(p) < amount) return false;
        return economy.withdrawPlayer(p, amount).transactionSuccess();
    }

    public double balance(Player p) {
        return economy.getBalance(p);
    }
}