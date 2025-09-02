package com.mclzyun.blog.playerDuel;
import com.mclzyun.blog.playerDuel.PlayerDuel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DuelCommand implements CommandExecutor {
    // 存储决斗请求，key 是被请求者，value 是请求者
    static final Map<String, String> duelRequests = new HashMap<>();
    private final File areasFile = new File(PlayerDuel.main.getDataFolder(), "areas.yml");
    private final YamlConfiguration areasConfig = YamlConfiguration.loadConfiguration(areasFile);
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // None
        if (args.length == 0) {
            String msg = PlayerDuel.lang.getString("PLAYERDUEL-USAGE");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        // help
        if (args.length == 1 && args[0].equals("help")) {
            sender.sendMessage("PlayerDuel 插件帮助 by ZherKing：\n" +
                    "/playerduel help - 显示帮助信息\n" +
                    "/playerduel duel <player> - 向指定玩家发起决斗请求\n" +
                    "/playerduel accept - 接受决斗请求\n" +
                    "/playerduel decline - 拒绝决斗请求");
            return true;
        }
    // duel <player>
        if (args.length == 2 && args[0].equals("duel")) {
            // 发起决斗请求的逻辑
            String targetPlayerName = args[1];
            // 判断玩家是否在线等逻辑
            if (!(sender instanceof org.bukkit.entity.Player)) {

                String msg = PlayerDuel.lang.getString("ONLY-PLAYERS-SEND-THE-DUEL-MSG");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            } // 不是玩家
            org.bukkit.entity.Player targetPlayer = org.bukkit.Bukkit.getPlayerExact(targetPlayerName);
            if (targetPlayer == null || !targetPlayer.isOnline()) {

                String msg = PlayerDuel.lang.getString("PLAYER-NOT-ONLINE");
                msg = msg.replace("%player%", targetPlayerName);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            } // 玩家不在线
            if (targetPlayer.getName().equals(sender.getName())) {

                String msg = PlayerDuel.lang.getString("YOU-CANNOT-DUEL-YOURSELF");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            } // 不能向自己发起决斗
            String msg = PlayerDuel.lang.getString("YOU-CANNOT-DUEL-YOURSELF");
            msg = msg.replace("%player%", targetPlayerName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

            duelRequests.put(targetPlayer.getName(), sender.getName());
            // 这里可以添加更多逻辑，比如存储请求，通知目标玩家等
            String msg1 = PlayerDuel.lang.getString("HAVE-PLAYER-SEND-THE-DUEL-REQUEST");
            msg1 = msg1.replace("%player%", sender.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg1));

            return true;
        }
    // accept
        if (args.length == 1 && args[0].equals("accept")) {
            String playerName = sender.getName();
            if (!(sender instanceof org.bukkit.entity.Player)) {

                String msg = PlayerDuel.lang.getString("ONLY-PLAYER-CAN-RESPOND-DUEL-REQUEST");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            } // 不是玩家
            if (!duelRequests.containsKey(playerName)) {

                String msg = PlayerDuel.lang.getString("YOU-HAVE-NO-PENDING-REQUESTS");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

                return true;
            }
            String challenger = duelRequests.remove(playerName);
            // 开始决斗逻辑
            String msg = PlayerDuel.lang.getString("YOU-ACCEPTED-REQUEST-FROM");
            msg = msg.replace("%player%", challenger);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

            Player challengerPlayer = Bukkit.getPlayerExact(challenger);
            Player acceptPlayer = (Player) sender;
            if (challengerPlayer == null || !challengerPlayer.isOnline()) {
                String msg1 = PlayerDuel.lang.getString("PLAYER-WENT-OFFLINE-AND-DUEL-IS-CANCELED");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg1));
                return true;
            }
            String msg1 = PlayerDuel.lang.getString("ACCEPT-DUEL-REQUEST");
            msg1 = msg1.replace("%player%", playerName);
            Objects.requireNonNull(Bukkit.getPlayerExact(challenger)).sendMessage(ChatColor.translateAlternateColorCodes('&', msg1));

            String arenaName = areasConfig.getKeys(false).isEmpty() ? "arena1" : areasConfig.getKeys(false).iterator().next();
            if (args.length >= 2) {
                arenaName = args[1]; // 玩家指定
            }
            Location spawn1 = getArenaSpawn(arenaName, 1);
            Location spawn2 = getArenaSpawn(arenaName, 2);

            if (spawn1 == null || spawn2 == null) {
                String msg2 = PlayerDuel.lang.getString("ARENA-RESPAWN-POINT-NOT-SET");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg2));
                return true;
            }
            challengerPlayer.teleport(spawn1);
            acceptPlayer.teleport(spawn2);
            new ArenaTimer(challengerPlayer, acceptPlayer, 5).runTaskTimer(PlayerDuel.main, 0L, 20L); // 丢到 AreanaTimer 来处理竞技
            return true;
        }
    // decline
        if (args.length == 1 && args[0].equals("decline")) {
            String playerName = sender.getName();
            if (!(sender instanceof org.bukkit.entity.Player)) {

                String msg2 = PlayerDuel.lang.getString("ONLY-PLAYER-CAN-DENY-DUEL-REQUEST");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg2));

                return true;
            } // 不是玩家
            if (!duelRequests.containsKey(playerName)) {

                String msg2 = PlayerDuel.lang.getString("NO-PENDING-REQUESTS-FROM");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg2));
                return true;
            }
            String challenger = duelRequests.remove(playerName);
            String denyMsg = PlayerDuel.lang.getString("YOU-DENIED-REQUEST-FROM").replace("%player%", challenger);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', denyMsg));

            String deniedMsg = PlayerDuel.lang.getString("PLAYER-DENIED-YOUR-DUEL-REQUEST").replace("%player%", playerName);
            Objects.requireNonNull(Bukkit.getPlayerExact(challenger)).sendMessage(ChatColor.translateAlternateColorCodes('&', deniedMsg));
            return true;
        }
    // reload
        if (args.length == 1 && args[0].equals("reload")) {
            String configReloadedMsg = PlayerDuel.lang.getString("PLUGIN-RELOADED");
            String langReloadedMsg = PlayerDuel.lang.getString("PLUGIN-LANGUAGE-RELOADED");
            PlayerDuel.main.reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configReloadedMsg));
            File langFile = new File(PlayerDuel.main.getDataFolder(), "language.yml");
            PlayerDuel.lang = YamlConfiguration.loadConfiguration(langFile);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', langReloadedMsg));
            return true;
        }
    // 竞技场相关命令
        if (args[0].equalsIgnoreCase("arena")) {
            if (args.length >= 3 && args[1].equalsIgnoreCase("create")) {
                String name = args[2];
                areasConfig.set(name, null);
                saveAreas();
                String msg = PlayerDuel.lang.getString("ARENA-CREATED").replace("%arena%", name);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("setspawn") && sender instanceof Player) {
                String name = args[2];
                int point = args[3].equals("1") ? 1 : 2;
                Player p = (Player) sender;
                Location loc = p.getLocation();
                String path = name + ".spawn" + point;
                areasConfig.set(path + ".world", loc.getWorld().getName());
                areasConfig.set(path + ".x", loc.getX());
                areasConfig.set(path + ".y", loc.getY());
                areasConfig.set(path + ".z", loc.getZ());
                areasConfig.set(path + ".yaw", loc.getYaw());
                areasConfig.set(path + ".pitch", loc.getPitch());
                saveAreas();
                String msg = PlayerDuel.lang.getString("ARENA-SPAWN-SET")
                        .replace("%arena%", name)
                        .replace("%point%", String.valueOf(point));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }
            String msg = PlayerDuel.lang.getString("ARENA-HELP");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            return true;
        }
        return false;
    }
    private void saveAreas() {
        try {
            areasConfig.save(areasFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 辅助方法
    private Location getArenaSpawn(String arenaName, int spawnNum) {
        String path = arenaName + ".spawn" + spawnNum;
        String worldName = areasConfig.getString(path + ".world");
        double x = areasConfig.getDouble(path + ".x");
        double y = areasConfig.getDouble(path + ".y");
        double z = areasConfig.getDouble(path + ".z");
        float yaw = (float) areasConfig.getDouble(path + ".yaw");
        float pitch = (float) areasConfig.getDouble(path + ".pitch");
        if (worldName == null) return null;
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }
}
