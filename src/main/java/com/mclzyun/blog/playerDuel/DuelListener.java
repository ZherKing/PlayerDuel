package com.mclzyun.blog.playerDuel;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Bukkit;

public class DuelListener implements Listener {
    private Player player1;
    private Player player2;

    // 判断玩家是否被击败
    private boolean duelEnded = false;

    public DuelListener() {
    }

    public void ArenaListener(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public DuelListener(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();
        DuelCommand.duelRequests.remove(name);
        // 也可以遍历移除所有以 name 为 value 的请求
        DuelCommand.duelRequests.values().removeIf(v -> v.equals(name));
    }

    // 监听玩家互相伤害事件，更新记分板上的血量显示
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        // 只处理决斗双方
        if ((damaged.equals(player1) && damager.equals(player2)) ||
                (damaged.equals(player2) && damager.equals(player1))) {

            double health1 = player1.getHealth() - (damaged.equals(player1) ? event.getFinalDamage() : 0);
            double health2 = player2.getHealth() - (damaged.equals(player2) ? event.getFinalDamage() : 0);

            // 攻击提示
            String damageMsg = PlayerDuel.lang.getString("DUEL-DAMAGE-MSG")
                    .replace("%player%", damaged.getName())
                    .replace("%damage%", String.valueOf(event.getFinalDamage()));
            damager.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', damageMsg));

            // 更新双方计分板
            updateHealthScoreboard(player1, health2, player2.getName());
            updateHealthScoreboard(player2, health1, player1.getName());

            // 判断是否击败
            if ((damaged.equals(player1) && health1 <= 0) || (damaged.equals(player2) && health2 <= 0)) {
                duelEnded = true;
                String winnerName = damager.getName();

                String winMsg = PlayerDuel.lang.getString("DUEL-WIN-MSG")
                        .replace("%player%", damaged.getName());
                damager.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winMsg));

                String loseMsg = PlayerDuel.lang.getString("DUEL-LOSE-MSG")
                        .replace("%player%", damager.getName());
                damaged.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', loseMsg));

                // 刷新计分板标题为胜者
                setWinnerScoreboard(player1, winnerName);
                setWinnerScoreboard(player2, winnerName);

                // 向双方宣布胜利者
                String winnerMsg = PlayerDuel.lang.getString("DUEL-WINNER-MSG")
                        .replace("%winner%", winnerName);
                player1.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winnerMsg));
                player2.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winnerMsg));

                // 延迟3秒传送
                Bukkit.getScheduler().runTaskLater(PlayerDuel.main, () -> {
                    player1.teleport(player1.getWorld().getSpawnLocation());
                    player2.teleport(player2.getWorld().getSpawnLocation());
                    clearScoreboard(player1);
                    clearScoreboard(player2);
                }, 60L); // 60 tick = 3秒
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player dead = event.getEntity();
        if (!duelEnded && (dead.equals(player1) || dead.equals(player2))) {
            duelEnded = true;
            Player winner = dead.equals(player1) ? player2 : player1;
            String loseMsg = PlayerDuel.lang.getString("DUEL-LOSE-MSG")
                    .replace("%player%", winner.getName());
            dead.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', loseMsg));

            String winMsg = PlayerDuel.lang.getString("DUEL-WIN-MSG")
                    .replace("%player%", dead.getName());
            winner.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winMsg));

            setWinnerScoreboard(player1, winner.getName());
            setWinnerScoreboard(player2, winner.getName());
            String winnerMsg = PlayerDuel.lang.getString("DUEL-WINNER-MSG")
                    .replace("%winner%", winner.getName());
            player1.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winnerMsg));
            player2.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', winnerMsg));

            Bukkit.getScheduler().runTaskLater(PlayerDuel.main, () -> {
                player1.teleport(player1.getWorld().getSpawnLocation());
                player2.teleport(player2.getWorld().getSpawnLocation());
                clearScoreboard(player1);
                clearScoreboard(player2);
            }, 60L);
        }
    }

    private void updateHealthScoreboard(Player player, double enemyHealth, String enemyName) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("duelCountdown");
        if (objective != null) {
            String healthScore = PlayerDuel.lang.getString("DUEL-HEALTH-SCORE")
                    .replace("%enemy%", enemyName);
            objective.getScore(healthScore).setScore((int) enemyHealth);
        }
    }
    // 设置胜者计分板标题
    private void setWinnerScoreboard(Player player, String winnerName) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("duelCountdown");
        if (objective != null) {
            String winnerTitle = PlayerDuel.lang.getString("DUEL-WINNER-TITLE")
                    .replace("%winner%", winnerName);
            objective.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', winnerTitle));
        }
    }
    // 清除计分板
    private void clearScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}

