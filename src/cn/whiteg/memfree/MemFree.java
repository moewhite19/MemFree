package cn.whiteg.memfree;

import cn.whiteg.memfree.Listener.EntityExplosion;
import cn.whiteg.memfree.Listener.MapUpdateListener;
import cn.whiteg.memfree.Listener.PlayerListener;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

import static cn.whiteg.memfree.Setting.DEBUG;

public class MemFree extends PluginBase {
    public static Logger logger;
    public static MemFree plugin;
    public MFRunnable timer = new MFRunnable(this);
    public CommandManage mainCmd;

    public MemFree() {
        plugin = this;
    }

    public void onLoad() {
        saveDefaultConfig();
        logger = getLogger();
    }

    public void onEnable() {
        Setting.reload();
        mainCmd = new CommandManage(this);
        mainCmd.setExecutor();
        regListener(new PlayerListener());
        regListener(new EntityExplosion());
        if (Setting.updateMapFileDate) regListener(new MapUpdateListener());
        if (DEBUG) getLogger().info("启用计时器");
        logger.info("已启用");
        //延迟启动
        Bukkit.getScheduler().runTask(this,() -> {
            timer.start();
        });
    }

    public void onDisable() {
        if (Bukkit.isStopping()) return; //当服务器正在关闭时不注销计时器

        unregListener();
        timer.stopTimer();
        timer = null;
        getLogger().info("已关闭");
    }
}