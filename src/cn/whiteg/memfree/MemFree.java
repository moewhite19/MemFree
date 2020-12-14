package cn.whiteg.memfree;

import cn.whiteg.memfree.Listener.MapUpdateListener;
import cn.whiteg.memfree.Listener.PlayerListener;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import java.util.logging.Logger;

import static cn.whiteg.memfree.Setting.*;

public class MemFree extends PluginBase {
    public static Logger logger;
    public static MemFree plugin;
    public MFRunnable timer = new MFRunnable(this);
    public CommandManage mainCmd;

    public MemFree() {
        plugin = this;
    }

    public void onLoad() {
        //getLogger().info("加载配置文件");
        saveDefaultConfig();
        minfree = CommonUtils.toByteLength(getConfig().getString("minfree","256m"));
        mintps = getConfig().getDouble("mintps");
    }

    public void onEnable() {
        logger = getLogger();
        Setting.reload();
        PluginCommand mfcmd = getCommand("MemFree");
        if (mfcmd != null){
            mainCmd = new CommandManage();
            mfcmd.setExecutor(mainCmd);
            mfcmd.setTabCompleter(mainCmd);
        }
        regListener(new PlayerListener());
        if (Setting.updateMapFileDate) regListener(new MapUpdateListener());
        if (DEBUG) getLogger().info("启用计时器");
        logger.info("已启用");
        //延迟启动
        Bukkit.getScheduler().runTask(this,() ->{
            timer.start();
        });
//        Bukkit.getScheduler().runTaskLater(this,() -> {
//            timer.start();
//        },100);
    }

    public void onDisable() {
        unregListener();
        timer.stopTimer();
        timer = null;
        getLogger().info("已关闭");
    }
}