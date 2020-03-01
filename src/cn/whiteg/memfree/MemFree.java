package cn.whiteg.memfree;

import cn.whiteg.memfree.Listener.limElytra;
import cn.whiteg.mmocore.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import static cn.whiteg.memfree.Setting.*;

public class MemFree extends JavaPlugin {
    public static Logger logger;
    public static MemFree plugin;
    static long runtime = 0;
    public final MFRunnable timer = new MFRunnable(this);
    public final Map<String, Listener> listenerMap = new HashMap<>();
    public CommandManage mainCmd;
    public SubCommand subCmds;

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
        //exitev = new PlayerListener();
        //Bukkit.getPluginManager().registerEvents(this.exitev,this);
        //getLogger().info("注册指令");
        mainCmd = new CommandManage();
        PluginCommand mfcmd = getCommand("MemFree");
        mfcmd.setExecutor(mainCmd);
        mfcmd.setTabCompleter(mainCmd);
        //regEven(new antiRedstone());
//        regEven(new PlayerListener());
        Setting.reload();
        if (Setting.limElytra > 0) regEven(new limElytra());
//        if (MaxEntity != null){
//            regEven(new EntitySpawn());
//        }
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
        regEven(listener.getClass().getName(),listener);
    }

    public void regEven(String key,Listener listener) {
        listenerMap.put(key,listener);
        Bukkit.getPluginManager().registerEvents(listener,plugin);
    }

    public void unregEven() {
        Iterator<Map.Entry<String, Listener>> it = listenerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Listener> set = it.next();
            Listener listener = set.getValue();
            if (listener != null){
                unregListener(listener);
            }
        }
    }

    /**
     * 卸载事件
     *
     * @param Key "卸载"
     */
    public void unregEven(String Key) {
        Listener listenr = listenerMap.remove(Key);
        if (listenr == null){
            return;
        }
        unregListener(listenr);
    }

    public void unregListener(Listener listener) {
        //注销事件
        Class listenerClass = listener.getClass();
        try{
            for (Method method : listenerClass.getMethods()) {
                if (method.isAnnotationPresent(EventHandler.class)){
                    Type[] tpyes = method.getGenericParameterTypes();
                    if (tpyes.length == 1){
                        Class<?> tc = Class.forName(tpyes[0].getTypeName());
                        Method tm = tc.getMethod("getHandlerList");
                        HandlerList handlerList = (HandlerList) tm.invoke(null);
                        handlerList.unregister(listener);
                    }
                }
            }
        }catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e){
            e.printStackTrace();
        }

        //调用类中的unreg()方法
        try{
            Method unreg = listenerClass.getDeclaredMethod("unreg");
            unreg.setAccessible(true);
            unreg.invoke(listener);
        }catch (Exception e){
        }
    }
}