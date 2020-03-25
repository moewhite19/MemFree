package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.apache.logging.log4j.LogManager.getLogger;

public class MFRunnable {
    private static Thread mfThread;
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
    private Thread mainThread = null;
    private DenyRestart restartTask = null;

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
//                    if(!isTimer) cancel();
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
                            List<Entity> entitys = player.getNearbyEntities(Setting.MaxSpawn_Range,512D,Setting.MaxSpawn_Range);

                            if (Setting.clearAI){
                                EnumMap<EntityType, Integer> num = new EnumMap<>(EntityType.class);
                                boolean f = false;
                                Iterator<Entity> ite = entitys.iterator();
                                while (ite.hasNext()) {
                                    Entity e = ite.next();
                                    if (e == null) continue;
                                    EntityType type = e.getType();
                                    if (type == EntityType.PLAYER) continue;
                                    Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                                    Integer i = num.getOrDefault(type,0) + 1;
                                    if (i > lim){
                                        if (e instanceof Mob){
                                            MonitorUtil.clearEntityAI((Mob) e);
                                            f = true;
                                            ite.remove();
                                        } else {
                                            e.remove();
                                        }
                                    } else {
                                        num.put(type,i);
                                    }
                                }
                                if (f){
                                    for (Entity e : entitys) {
                                        EntityType type = e.getType();
                                        Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                                        Integer i = num.getOrDefault(type,0) + 1;
                                        num.put(type,i);
                                        if (i >= lim && e instanceof Mob){
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
//                            player.sendMessage("搜索完成 耗时" + (System.currentTimeMillis() - startT) + "毫秒");
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
                            if (Setting.AutoRestart){
                                if (mainThread != null){
                                    try{
                                        mainThread.stop();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                System.exit(9);
                            } else MemFree.logger.warning("服务器线程堵塞?");
                        }
                        if ((st - updateTime) > tick * 2){
                            warin++;
                            if (warin > maxwarin){
                                if (Setting.AutoRestart) denyShwtdown();
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
                                if (Setting.AutoRestart) denyShwtdown();
                                else MemFree.logger.warning("服务器可能需要重启");
                            }
                        } else if (tps < mintps){
                            warin++;
                            if (warin > maxwarin){
                                if (Setting.AutoRestart) denyShwtdown();
                                else MemFree.logger.warning("服务器可能需要重启");
                            }
                        } else if (warin > 0) warin--;
                        if (Setting.enableAutoClearUpWorld){
                            long free = Bukkit.getServer().getWorldContainer().getFreeSpace();
                            if (free < Setting.autoClearMinFreeSpace){
                                MemFree.logger.info("磁盘剩余空间 " + CommonUtils.tanByte(free) + "开始自动清理世界");
                                File[] files = new File[Setting.worldClearUpNumber];
                                Long[] modifiedCache = new Long[files.length];
                                long size = 0;
                                long now = System.currentTimeMillis();
                                for (int wi = 0; wi < Setting.autoClearUpWorlds.length; wi++) {
                                    File dir = Setting.autoClearUpWorlds[wi];
                                    long offset = Setting.autoClearUpWhordsOffset[wi];
                                    if (!dir.isDirectory()) continue;
                                    for (File region : Objects.requireNonNull(dir.listFiles())) {
                                        for (int i = 0; i < files.length; i++) {
                                            long modif = region.lastModified() + offset;
                                            if (modif < now && (files[i] == null || modif < modifiedCache[i])){
                                                files[i] = region;
                                                modifiedCache[i] = modif;
                                                break;
                                            }
                                        }
                                    }
                                }

                                for (File f : files) {
                                    if (f == null) continue;
                                    String name = f.getName();
                                    long l = f.length();
                                    if (!f.delete()) continue;
                                    size += l;
                                    size += f.length();
                                    File poidir = new File(f.getParentFile().getParentFile(),"poi");
                                    if (poidir.isDirectory()){
                                        File poi = new File(poidir,name);
                                        size += poi.length();
                                        if (poi.exists()){
                                            l = poi.length();
                                            if (!poi.delete()) continue;
                                            size += l;
                                        }
                                    }
                                }
                                if (size > 0)
                                    MemFree.logger.info("共清理了" + CommonUtils.tanByte(size));
                                else MemFree.logger.info("没有清理掉任何文件");
                            }
                        }

                    }catch (Throwable e){
                        MemFree.logger.info("计时器错误" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            mfThread.setName("MemFreeTimer");
            mfThread.setDaemon(true);
            mfThread.start();
            Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
                mainThread = Thread.currentThread();
                MemFree.logger.info("已获取到主线程: " + mainThread.getName());
            });
        }
    }


    public void Restart() {
        stopRestart();
        List<String> commands = plugin.getConfig().getStringList("onCommands");
        new BukkitRunnable() {
            Iterator<String> it = commands.iterator();

            @Override
            public void run() {
                if (it.hasNext()){
                    String cmd = it.next();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd.replace('&','§'));
                } else {
                    cancel();
                    it = null;
                }
            }
        }.runTaskTimer(MemFree.plugin,1L,1L);
//        stopTimer();
//        sendCommand(commands);
    }

    public void denyShwtdown(int dny) {
        stopRestart();
        new DenyRestart(dny);
    }

    public void denyShwtdown() {
        if (restartTask != null){
            return;
        }
        int time = Setting.restartDeny;
        denyShwtdown(time);
    }

    public void stopTimer() {
        if (Runer == null) return;
        Runer.cancel();
        Runer = null;
//        try{
//            mfThread.stop();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
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

    public boolean hasRestart() {
        return restartTask != null;
    }

    public boolean stopRestart() {
        if (restartTask != null){
            restartTask.stop();
            return true;
        }
        return false;
    }

    public Thread getMainThread() {
        return mainThread;
    }

    public class DenyRestart extends BukkitRunnable {
        final int time;
        private final BossBar bar;
        int dny;

        public DenyRestart(int s) {
            time = s;
            dny = s;
            bar = Bukkit.createBossBar("重启倒计时",BarColor.WHITE,BarStyle.SOLID);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                bar.addPlayer(onlinePlayer);
                onlinePlayer.sendMessage("服务器将会在" + time + "秒后重启");
            }
            runTaskTimer(MemFree.plugin,20L,20L);
            restartTask = this;
        }

        @Override
        public void run() {
            dny--;
            if (dny <= 0){
                cancel();
                bar.removeAll();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.kickPlayer("§f服务器似乎内存满了,\n请等待1分钟服务器重启完成后再加入服务器. \n§l期待与您再见.");
                }
                Restart();
                stop();
            } else {
                bar.setProgress(((double) dny) / time);
            }
        }

        public void stop() {
            bar.removeAll();
            cancel();
            restartTask = null;
        }
    }
}
