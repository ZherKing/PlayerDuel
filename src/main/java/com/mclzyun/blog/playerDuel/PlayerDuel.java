package com.mclzyun.blog.playerDuel;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public final class PlayerDuel extends JavaPlugin {
    public static YamlConfiguration lang;
    static PlayerDuel main;

    @Override
    public void onEnable() {
        main = this;
        // Plugin startup logic
        // 欢迎信息
        getLogger().info("PlayerDuel 插件已启用！");
        getLogger().info("欢迎使用 PlayerDuel 插件！by 真境");
        getLogger().info("如果你喜欢这个插件，请给个好评！");
        // 注册命令
        this.getCommand("playerduel").setExecutor(new DuelCommand());
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new DuelListener(), this);
        // 载入语言文件
        File langFile = new File(getDataFolder(), "language.yml");
        if (!langFile.exists()) {
            saveResource("language.yml", false);
        }
        lang = YamlConfiguration.loadConfiguration(langFile);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("PlayerDuel 插件已禁用！");
    }
}
