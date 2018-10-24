package cn.whiteg.memfree;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MemFree extends JavaPlugin {
    MFRunnable timer = new MFRunnable();
    private playerexit exitev;
    long minfree;
    private static MemFree INSTANCE;

    public MemFree() {
    }
    public void onLoad() {
        getLogger().info("加载配置文件");
        saveDefaultConfig();
        minfree = getConfig().getLong("minfree") * 1024 * 1024;
    }

    public void onEnable() {
        INSTANCE = this;
        //exitev = new playerexit();
        //Bukkit.getPluginManager().registerEvents(this.exitev,this);
        getCommand("MemFree").setExecutor(new CommandManager());
        timer.setTimer();
        getLogger().info("已启用");
    }

    public void onDisable() {
        InventoryClickEvent.getHandlerList().unregister(exitev);
        timer.stopTimer();
        getLogger().info("已关闭");
    }

    public static MemFree in() {
        return INSTANCE;
    }
}