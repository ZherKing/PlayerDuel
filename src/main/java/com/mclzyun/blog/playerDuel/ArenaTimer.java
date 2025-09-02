package com.mclzyun.blog.playerDuel;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import net.md_5.bungee.api.ChatColor;

// 处理决斗倒计时的类
public class ArenaTimer extends BukkitRunnable {
    private final Player player1;
    private final Player player2;
    private int time;
// 构造函数，初始化玩家和倒计时秒数
    public ArenaTimer(Player player1, Player player2, int countdownSeconds) {
        this.player1 = player1;
        this.player2 = player2;
        this.time = countdownSeconds;
        setupScoreboard();
    }
// 设置记分板显示倒计时
private void setupScoreboard() {
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    String title = PlayerDuel.lang.getString("DUEL-COUNTDOWN-TITLE");
    Objective objective = scoreboard.registerNewObjective("duelCountdown", "dummy", ChatColor.translateAlternateColorCodes('&', title)); // "dummy" 类型 表示手动更新
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    player1.setScoreboard(scoreboard);
    player2.setScoreboard(scoreboard);
}

    @Override
    public void run() {
        Scoreboard scoreboard = player1.getScoreboard();
        Objective objective = scoreboard.getObjective("duelCountdown");
        if (time <= 0) {
            if (objective != null) {
                String startTitle = PlayerDuel.lang.getString("DUEL-START-TITLE");
                String startMsg = PlayerDuel.lang.getString("DUEL-START-MSG");
                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', startTitle));
                objective.getScore(ChatColor.translateAlternateColorCodes('&', startMsg)).setScore(0);
            }
            this.cancel();
            // 倒计时结束，开始决斗
            // 在决斗开始时注册监听器
            // 决斗开始时，切换计分板标题
            // 倒计时结束，清除计分板
            player1.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            player2.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            if (objective != null) {
                String scoreboardTitle = PlayerDuel.lang.getString("DUEL-SCOREBOARD-TITLE");
                objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', scoreboardTitle));
                // 可以设置其他分数项，如双方血量等
            }

            Bukkit.getPluginManager().registerEvents(new DuelListener(player1, player2), PlayerDuel.main);
            return;
        }
        if (objective != null) {
            String countdownTitle = PlayerDuel.lang.getString("DUEL-COUNTDOWN-TITLE");
            objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', countdownTitle));

            // 清除所有旧分数项
            for (String entry : objective.getScoreboard().getEntries()) {
                objective.getScoreboard().resetScores(entry);
            }
            // 设置当前倒计时分数项
            String timeMsg = PlayerDuel.lang.getString("DUEL-COUNTDOWN-TIME")
                    .replace("%time%", String.valueOf(time));
            objective.getScore(ChatColor.translateAlternateColorCodes('&', timeMsg)).setScore(time);
        }
        time--;
    }
}