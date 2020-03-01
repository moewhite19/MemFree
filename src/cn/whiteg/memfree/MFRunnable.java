package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class MFRunnable {
    static Thread mfThread;
    final public long max;
    public boolean isTimer;
    public volatile long Mem = 0;
    public volatile float tps = 20;
    public short warin = 0;
    public volatile long use;
    private MemFree plugin;
    private long date = 0;
    private long updateTime = 0;
    private BukkitTask Runer;
    private short maxwarin;
    private long runTick = 2;
    private long lastGcTime;

    public MFRunnable(MemFree me) {
        plugin = me;
        max = Runtime.getRuntime().maxMemory();
        lastGcTime = System.currentTimeMillis();
    }


    public void setTimer() {
        if (isTimer){
            getLogger().info("§b错误！  计时器已经启用");
        } else {
            getLogger().info("启动计时器");
            isTimer = true;
            Runer = new BukkitRunnable() {
                Iterator<? extends Player> it = null;

                @Override
                public void run() {
                    //getLogger().info("运行次数" + i++);
                    use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    Mem = max - use;
                    updateTime = System.currentTimeMillis();
                    long runTime = updateTime - date;
                    date = System.currentTimeMillis();
                    //Mem = (Mem + free) / 2;
                    tps = runTick * 1000 / ((float) runTime) * 20;
                    if (Setting.MaxEntity != null){
                        if (it != null && it.hasNext()){
                            Player player = it.next();
                            if (!player.isOnline() || player.isDead()) return;
                            List<Entity> entitys = player.getNearbyEntities(Setting.MaxSpawn_Range,Setting.MaxSpawn_Range,Setting.MaxSpawn_Range);
                            if (Setting.clearAI){
                                EnumMap<EntityType, Integer> map = new EnumMap<>(EntityType.class);
                                for (Entity e : entitys) {
                                    if (e instanceof Mob){
                                        LivingEntity le = (LivingEntity) e;
                                        EntityType type = e.getType();
                                        if (type == EntityType.PLAYER) continue;
                                        Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                                        Integer i = map.getOrDefault(type,0) + 1;
                                        map.put(type,i);
                                        if (i > lim){
                                            MonitorUtil.clearEntityAI((Mob) e);
                                        }
                                    }
                                }
                            } else {
                                EnumMap<EntityType, Integer> map = new EnumMap<>(EntityType.class);
                                for (Entity e : entitys) {
                                    EntityType type = e.getType();
                                    if (type == EntityType.PLAYER) continue;
                                    Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                                    Integer i = map.getOrDefault(type,0) + 1;
                                    if (i > lim){
                                        e.remove();
                                    } else {
                                        map.put(type,i);
                                    }
                                }
                            }

                        } else {
                            it = Bukkit.getOnlinePlayers().iterator();
                        }
                    }
                    //getLogger().info("最小内存" + minfree / 1024 / 1024);
                    //getLogger().info("剩余内存" + free / 1024 / 1024);
                }
            }.runTaskTimer(plugin,20 * runTick,20 * runTick);
            long minfree = Setting.minfree;
            double mintps = Setting.mintps;
            mfThread = new Thread(() -> {
                final long tick = runTick * 1000;
                warin = 0;
                maxwarin = Setting.Max_Warin;
                try{
                    Thread.sleep(5000L);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                while (isTimer) {
                    try{
                        long st = System.currentTimeMillis();
                        Thread.sleep(tick);
                        if (st - updateTime > 120000){
                            if (Setting.AutoRestart) System.exit(9);
                            else MemFree.logger.warning("服务器线程堵塞?");
                        }
                        if ((st - updateTime) > tick * 2){
                            warin++;
                            if (warin > maxwarin){
                                if (Setting.AutoRestart) Restart();
                                else MemFree.logger.warning("服务器可能需要重启");
                            }
                            System.out.print("卡顿警告");
                            tps = 0;
                            continue;
                        }
                        if (Mem < minfree){
                            if (Setting.DEBUG) MemFree.logger.info("内存警告");
                            warin++;
                            long now = System.currentTimeMillis();
                            if (Setting.autoGc && warin > Setting.gcMinWarin && (now - lastGcTime) > Setting.gcMinTick){
                                lastGcTime = now;
                                Bukkit.broadcastMessage("服务器开始强制回收内存,可能会有短暂卡顿");
                                long n = System.currentTimeMillis();
                                final Runtime r = Runtime.getRuntime();
                                final long m = r.freeMemory();
                                System.gc();
                                long now_m = r.freeMemory() - m;
                                Bukkit.broadcastMessage("内存回收完成,回收了" + now_m / 1024 / 1024 + "MB内存 耗时" + (System.currentTimeMillis() - n) + "ms");
                                warin = 0;
                                continue;
                            }
                            if (warin > maxwarin){
                                if (Setting.AutoRestart) Restart();
                                else MemFree.logger.warning("服务器可能需要重启");
                            }
                        } else if (tps < mintps){
                            warin++;
                            if (warin > maxwarin){
                                if (Setting.AutoRestart) Restart();
                                else MemFree.logger.warning("服务器可能需要重启");
                            }
                        } else if (warin > 0) warin--;
                    }catch (Exception e){
                        MemFree.logger.info("计时器错误" + e.getMessage());
                    }
                }
            });
            mfThread.setName("MemFreeTimer");
            mfThread.setDaemon(true);
            mfThread.start();
        }
    }


    public void Restart() {
        List<String> commands = plugin.getConfig().getStringList("commands");
        Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
            sendCommand(commands);
        });
        denyShwtdown();
        MemFree.logger.info("TPS " + tps);
        MemFree.logger.info("剩余内存 " + Mem / 1024 / 1024 + "MB");
        warin = 0;
    }

    public void denyShwtdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
//                long time = System.currentTimeMillis();
//                for (Player player : Bukkit.getOnlinePlayers()) {
//                    if (player.isOnline()) player.kickPlayer("服务器正在重启");
//                }
//                for (World world : Bukkit.getWorlds()) {
//                    for (Chunk chunk : world.getLoadedChunks()) {
//                        chunk.unload(true);
//                    }
//                }
//                MemFree.logger.info("耗时: " + String.valueOf(System.currentTimeMillis() - time) + "ms");
                List<String> commands = plugin.getConfig().getStringList("onCommands");
                sendCommand(commands);
            }
        }.runTaskLater(plugin,20 * plugin.getConfig().getLong("onTime"));
    }

    public void stopTimer() {
        if (Runer == null) return;
        Runer.cancel();
        Runer = null;
        mfThread = null;
        isTimer = false;
        getLogger().info("已关闭计时器");
    }


    public void sendCommand(List<String> commands) {
        for (int i = 0; i < commands.size(); i++) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),commands.get(i).replace("&","§"));
            //stopTimer();
        }
    }

}
