package cn.whiteg.memfree;

import cn.whiteg.memfree.Listener.EntitySpawn;
import cn.whiteg.memfree.Listener.limElytra;
import cn.whiteg.mmocore.MMOCore;
import cn.whiteg.mmocore.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static cn.whiteg.memfree.Setting.*;

public class MemFree extends JavaPlugin {
    static long runtime = 0;
    public final MFRunnable timer = new MFRunnable(this);
    public static Logger logger;
    public static MemFree plugin;
    public final Map<String, Listener> listenerMap = new HashMap<>();
    public CommandManage mainCmd;
    public SubCommand subCmds;
    public MemFree(){
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
        //exitev = new playerexit();
        //Bukkit.getPluginManager().registerEvents(this.exitev,this);
        //getLogger().info("注册指令");
        mainCmd = new CommandManage();
        PluginCommand mfcmd = getCommand("MemFree");
        mfcmd.setExecutor(mainCmd);
        mfcmd.setTabCompleter(mainCmd);
        //regEven(new antiRedstone());
        Setting.reload();
        if (Setting.limElytra > 0) regEven(new limElytra());
        if (getConfig().getBoolean("MaxSpawn.Enable")){
            regEven(new EntitySpawn());
        }
        if (DEBUG) getLogger().info("启用计时器");
        if (runtime == 0) runtime = ManagementFactory.getRuntimeMXBean().getStartTime();
        logger.info("已启用");
        subCmds = new SubCommand();
        Bukkit.getScheduler().runTaskLaterAsynchronously(this,() -> {
            for (String cmd : subCmds.subCmds) {
                PluginCommand pc = PluginUtil.getPluginCommanc(this,cmd);
                if (pc == null){
                    logger.info("没有注册指令 " + cmd);
                    continue;
                }
                pc.setExecutor(subCmds);
                pc.setTabCompleter(subCmds);
            }
            timer.setTimer();
        },200L);
    }

    public void onDisable() {
        //InventoryClickEvent.getHandlerList().unregister(exitev);
        unregEven();
        timer.stopTimer();
        getLogger().info("已关闭");
    }

    public void regEven(Listener listener) {
        String key = listener.getClass().getName();
        logger.info("注册事件:" + key);
        Bukkit.getPluginManager().registerEvents(listener,plugin);
    }

    public void unregEven() {
        for (Map.Entry<String, Listener> entry : listenerMap.entrySet()) {
            unregEven(entry.getKey());
        }
    }

    public void unregEven(String Key) {
        if (listenerMap.get(Key) == null){
            return;
        }
        Listener evens = listenerMap.get(Key);
        try{
            Class c = evens.getClass();
            Method unreg = c.getDeclaredMethod("unreg");
            unreg.setAccessible(true);
            unreg.invoke(evens);
        }catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e){
            e.printStackTrace();
        }
    }
}