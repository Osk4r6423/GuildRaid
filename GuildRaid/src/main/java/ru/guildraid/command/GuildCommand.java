package ru.guildraid.command;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.guildraid.GuildRaidPlugin;
import ru.guildraid.gui.GuildMenu;

import java.util.*;
import java.util.stream.Collectors;

public class GuildCommand implements CommandExecutor, TabCompleter {
    private final GuildRaidPlugin plugin;

    public GuildCommand(GuildRaidPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu")) {
            if (!player.hasPermission("guild.member")) {
                plugin.msg().send(player, "no-permission");
                return true;
            }
            new GuildMenu(plugin, player).open();
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "warp" -> plugin.guilds().warp(player);
            case "raid" -> handleRaid(player, args);
            case "admin" -> handleAdmin(player, args);
            default -> new GuildMenu(plugin, player).open();
        }
        return true;
    }

    private void handleRaid(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("/guild raid <start|join|defend|suppress|ransom>");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "start" -> {
                if (args.length < 3) {
                    player.sendMessage("/guild raid start <гильдия>");
                    return;
                }
                plugin.raids().startRaid(player, args[2]);
            }
            case "join" -> plugin.raids().joinRaider(player);
            case "defend" -> plugin.raids().joinDefender(player);
            case "suppress" -> plugin.raids().suppressLife(player);
            case "ransom" -> {
                if (args.length < 3) {
                    player.sendMessage("/guild raid ransom <сумма>");
                    return;
                }
                try {
                    plugin.raids().setRansom(player, Double.parseDouble(args[2]));
                } catch (NumberFormatException ex) {
                    player.sendMessage("Неверная сумма");
                }
            }
            default -> player.sendMessage("/guild raid <start|join|defend|suppress|ransom>");
        }
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("guild.admin")) {
            plugin.msg().send(player, "no-permission");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("/guild admin <reload|setwarp|setleader|joinguild>");
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.msg().send(player, "reload");
            }
            case "setwarp" -> {
                if (args.length < 3) {
                    player.sendMessage("/guild admin setwarp <гильдия>");
                    return;
                }
                plugin.guilds().setWarp(args[2], player.getLocation());
                plugin.msg().send(player, "setwarp", Map.of("guild", args[2]));
            }
            case "joinguild" -> {
                if (args.length < 3) return;
                plugin.guilds().joinGuild(player, args[2]);
                player.sendMessage("Вы вступили в " + args[2]);
            }
            case "setleader" -> {
                if (args.length < 3) return;
                var g = plugin.storage().getGuild(args[2]);
                if (g != null) {
                    g.setLeader(player.getUniqueId());
                    g.getMembers().add(player.getUniqueId());
                    plugin.storage().getPlayer(player.getUniqueId()).setGuildId(g.getId());
                    player.sendMessage("Вы лидер " + args[2]);
                }
            }
            default -> player.sendMessage("Неизвестная admin-команда");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("menu", "warp", "raid", "admin"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("raid")) {
            return filter(List.of("start", "join", "defend", "suppress", "ransom"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("raid") && args[1].equalsIgnoreCase("start")) {
            return filter(new ArrayList<>(plugin.configs().guildDefs().keySet()), args[2]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return filter(List.of("reload", "setwarp", "joinguild", "setleader"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            return filter(new ArrayList<>(plugin.configs().guildDefs().keySet()), args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> src, String token) {
        String t = token.toLowerCase(Locale.ROOT);
        return src.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(t)).collect(Collectors.toList());
    }
}