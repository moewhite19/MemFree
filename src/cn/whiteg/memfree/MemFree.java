package cn.whiteg.memfree;

import cn.whiteg.memfree.Listener.limElytra;
import org.bukkit.command.PluginCommand;

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import static cn.whiteg.memfree.Setting.*;

public class MemFree extends PluginBase {
    public static Logger logger;
    public static MemFree plugin;
    static long runtime = 0;
    public MFRunnable timer = new MFRunnable(this);
    public CommandManage mainCmd;

    public MemFree() {
        plugin = this;
    }

    public void onLoad() {
        //getLogger().info("加载配置文件");
        saveDefaultConfig();
        minfree = getConfig().getLong("minfree") * 1024 * 1024;
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
        if (Setting.limElytra > 0) regListener(new limElytra());
        if (DEBUG) getLogger().info("启用计时器");
        if (runtime == 0) runtime = ManagementFactory.getRuntimeMXBean().getStartTime();
        logger.info("已启用");
        timer.setTimer();
    }

    public void onDisable() {
        //InventoryClickEvent.getHandlerList().unregister(exitev);
        unregListener();
        timer.stopTimer();
        timer = null;
        getLogger().info("已关闭");
    }
}