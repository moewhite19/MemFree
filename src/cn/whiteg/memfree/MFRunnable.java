package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class MFRunnable extends BukkitRunnable {
    private static MFThread mfThread;
    final public long max;
    private final Logger logger;
    public boolean isRun;
    public volatile long Mem = 0;
    public volatile float tps = 20;
    public short warin = 0;
    public volatile long use;
    Iterator<? extends Player> playersIt = Bukkit.getOnlinePlayers().iterator();
    private MemFree plugin;
    private long date = 0;
    private long updateTime = 0;
    private BukkitTask Runer;
    private short maxwarin;
    private long runTick = 2;
    private long lastGcTime;
    private DenyRestart denyTask = null;

    public MFRunnable(MemFree me) {
        plugin = me;
        max = Runtime.getRuntime().maxMemory();
        lastGcTime = System.currentTimeMillis();
        logger = me.getLogger();
    }


    public void start() {
        if (isRun){
            logger.info("§b错误！  计时器已经启用");
        } else {
            logger.info("启动计时器");
            isRun = true;
            runTaskTimer(plugin,20 * runTick,20 * runTick);
            mfThread = new MFThread();
            mfThread.start();
        }
    }


    public void onRestart() {
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

    public void denyShwtdown(long dny) {
        stopRestart();
        new DenyRestart(dny);
    }

    public void denyShwtdown(long dny,long time) {
        stopRestart();
        new DenyRestart(dny,time);
    }

    public void denyShwtdown() {
        if (denyTask != null){
            return;
        }
        int time = Setting.restartDeny;
        denyShwtdown(time);
    }

    public void stopTimer() {
        if (Runer == null) return;
        Runer.cancel();
        Runer = null;
        mfThread = null;
        isRun = false;
        logger.info("已关闭计时器");
    }


    public boolean hasRestart() {
        return denyTask != null;
    }

    public DenyRestart getDenyTask() {
        return denyTask;
    }

    public boolean stopRestart() {
        if (denyTask != null){
            denyTask.stop();
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Mem = max - use;
        updateTime = System.currentTimeMillis();
        long runTime = updateTime - date;
        date = System.currentTimeMillis();
        //Mem = (Mem + free) / 2;
        tps = runTick * 1000 / ((float) runTime) * 20;
        if (Setting.MaxEntity != null){
            if (playersIt.hasNext()){
                Player player = playersIt.next();
                if (!player.isOnline() || player.isDead()) return;
                List<Entity> entitys = player.getNearbyEntities(Setting.MaxEntity_Range,512D,Setting.MaxEntity_Range);
                if (Setting.clearAI){
                    EnumMap<EntityType, Integer> map = new EnumMap<>(EntityType.class);
                    boolean f = false;
                    Iterator<Entity> ite = entitys.iterator();
                    while (ite.hasNext()) {
                        Entity e = ite.next();
                        if (e instanceof Player) continue;
                        EntityType type = e.getType();
                        Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                        Integer i = map.getOrDefault(type,0) + 1;
                        if (i > lim){
                            if (e instanceof Villager){
                                //村民没法删除AI  那就给他凋零效果吧
                                ((Villager) e).addPotionEffect(new PotionEffect(PotionEffectType.WITHER,100,1));
//                                            e.remove();
                            } else if (e instanceof Mob){
                                MonitorUtil.clearEntityAI((Mob) e);
                                f = true;
                                ite.remove();
                            } else {
                                e.remove();
                            }
                        } else {
                            map.put(type,i);
                        }
                    }
                    if (f){
                        for (Entity e : entitys) {
                            EntityType type = e.getType();
                            Integer lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                            Integer i = map.getOrDefault(type,0) + 1;
                            map.put(type,i);
                            if (i >= lim && e instanceof Mob){
                                MonitorUtil.clearEntityAI((Mob) e);
                            }

                        }
                    }
                } else {
                    EnumMap<EntityType, Integer> map = new EnumMap<>(EntityType.class);
                    for (Entity e : entitys) {
                        if (e instanceof Player) continue;
                        EntityType type = e.getType();
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
                playersIt = Bukkit.getOnlinePlayers().iterator();
            }
        }

        //getLogger().info("最小内存" + minfree / 1024 / 1024);
        //getLogger().info("剩余内存" + free / 1024 / 1024);
    }

    public class DenyRestart extends BukkitRunnable {
        final long time;
        long last;
        long dny;
        private BossBar bar = null;

        public DenyRestart(long s) {
            if (s > 60000){
                time = 60000;
            } else {
                time = s;
            }
            dny = s;
            last = System.currentTimeMillis();
            runTaskTimer(MemFree.plugin,20L,20L);
            denyTask = this;
        }

        public DenyRestart(long s,long time) {
            this.time = time;
            dny = s;
            last = System.currentTimeMillis();
            runTaskTimer(MemFree.plugin,20L,20L);
            denyTask = this;
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            dny -= now - last;
            last = now;
            if (dny <= 0){
                cancel();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.kickPlayer("§f服务器似乎内存满了,\n请等待1分钟服务器重启完成后再加入服务器. \n§l期待与您再见.");
                }
                onRestart();
                stop();
            } else {
                if (bar != null){
                    bar.setProgress(((double) dny) / time);
                    bar.setTitle(getMsg());
                } else if (dny < time){
                    bar = Bukkit.createBossBar(getMsg(),BarColor.WHITE,BarStyle.SOLID);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        bar.addPlayer(onlinePlayer);
                        onlinePlayer.sendMessage("服务器将会在" + CommonUtils.tanMintoh(time) + "后重启");
                    }
                }
            }
        }

        public void stop() {
            if (bar != null) bar.removeAll();
            cancel();
            denyTask = null;
        }

        public BossBar getBar() {
            return bar;
        }

        String getMsg() {
            return "§b重启倒计时§f" + CommonUtils.tanMintoh(dny);
        }

        public long getDny() {
            return dny;
        }
    }

    public class MFThread extends Thread {
        MFThread() {
            setName("MemFreeTimer");
            setDaemon(true);
        }

        @Override
        public void run() {
            final long tick = runTick * 1000;
            warin = 0;
            maxwarin = Setting.Max_Warin;
            try{
                Thread.sleep(5000L);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            while (isRun) {
                try{
                    long st = System.currentTimeMillis();
                    Thread.sleep(tick);
                    if (st - updateTime > 120000){
                        if (Setting.AutoRestart){
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
                    if (Mem < Setting.minfree){
                        if (Setting.DEBUG) MemFree.logger.warning("内存警告");
                        warin++;
                        long now = System.currentTimeMillis();
                        if (Setting.autoGc && now > lastGcTime && warin >= Setting.gcMinWarin){
                            Bukkit.broadcastMessage("服务器开始强制回收内存,可能会有短暂卡顿");
                            lastGcTime = now + Setting.gcMinTick;
                            long n = System.currentTimeMillis();
                            final Runtime r = Runtime.getRuntime();
                            final long m = r.freeMemory();
                            System.gc();
                            long now_m = r.freeMemory() - m;
                            warin = 0;
                            Bukkit.broadcastMessage("内存回收完成,回收了" + now_m / 1024 / 1024 + "MB内存 耗时" + (System.currentTimeMillis() - n) + "ms");
                            continue;
                        }
                        if (warin > maxwarin){
                            if (Setting.AutoRestart) denyShwtdown();
                            else MemFree.logger.warning("服务器可能需要重启");
                        }
                    } else if (tps < Setting.mintps){
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
            logger.info("线程已关闭");
        }
    }
}
