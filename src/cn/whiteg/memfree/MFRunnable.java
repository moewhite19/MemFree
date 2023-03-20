package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MFRunnable implements Listener {
    private static MFThread mfThread;
    final public long max;
    private final Logger logger;
    private final MemFree plugin;
    private final long tickInterval = 2; //执行间隔（单位:秒）
    private final AtomicInteger chunkLoad = new AtomicInteger();
    private final AtomicInteger chunkGenerate = new AtomicInteger();
    public boolean isRun;
    public long mem = 0;
    public volatile float tps = 20;
    public short warin = 0;
    Iterator<? extends Player> playersIt = Bukkit.getOnlinePlayers().iterator();
    boolean taskIsDone = false;
    private BukkitTask bukkitTask;
    private long lastUpdateTime = System.currentTimeMillis();
    private long lastGcTime;
    private DenyRestart denyTask = null;
    private int lastChunkLoad;
    private int lastGenerate;


    public MFRunnable(MemFree me) {
        plugin = me;
        max = Runtime.getRuntime().maxMemory();
        lastGcTime = System.currentTimeMillis();
        logger = me.getLogger();
    }

    public void start() {
        if (!isRun){
            logger.info("启动计时器");
            isRun = true;
            bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin,this::task,20 * tickInterval,20 * tickInterval);
            mfThread = new MFThread();
            mfThread.start();
            plugin.regListener(this);
        }
    }

    public boolean stopRestart() {
        if (denyTask != null){
            denyTask.stop();
            denyTask = null;
            return true;
        }
        return false;
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
        if (denyTask == null){
            denyTask = new DenyRestart(dny);
        } else {
            logger.warning("延时重启已存在");
        }
    }

    public void denyShwtdown(long dny,long time) {
        if (denyTask == null){
            denyTask = new DenyRestart(dny,time);
        } else {
            logger.warning("延时重启已存在");
        }
    }


    public void denyShwtdown() {
        if (denyTask != null){
            return;
        }
        denyShwtdown(Setting.restartDeny);
    }

    public void stopTimer() {
        if (bukkitTask == null) return;
        bukkitTask.cancel();
        bukkitTask = null;
        mfThread = null;
        isRun = false;
        HandlerList.unregisterAll(this);
        logger.info("已关闭计时器");
    }


    public boolean hasRestart() {
        return denyTask != null;
    }

    public DenyRestart getDenyTask() {
        return denyTask;
    }

    public void task() {
        var use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        mem = max - use;
        var now = System.currentTimeMillis();
        long interval = now - lastUpdateTime;
        lastUpdateTime = System.currentTimeMillis();
        tps = tickInterval * 1000 / ((float) interval) * 20;
        //检查密集实体
        //TODO 模块化成runnable包
        if (Setting.MaxEntity != null){
            if (playersIt.hasNext()){
                Player player = playersIt.next();
                if (!player.isOnline() || player.isDead() || player.getViewDistance() < player.getWorld().getViewDistance()) return; //如果玩家不在线，或者视距小于世界视距跳出
                List<Entity> entitys = player.getNearbyEntities(Setting.MaxEntity_Range,512D,Setting.MaxEntity_Range);
                if (Setting.clearAI){
                    boolean flag = false;
                    EnumMap<EntityType, Integer> map = new EnumMap<>(EntityType.class);
                    for (Entity entity : entitys) {
                        if(entity instanceof HumanEntity) continue;
                        EntityType type = entity.getType();
                        int lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                        int i = map.getOrDefault(type,0) + 1;
                        map.put(type,i);
                        if(! flag && i > lim) flag = true; //如果有超出限制的实体，下面重新遍历一遍
                    }
                    if (flag){
                        for (Entity e : entitys) {
                            EntityType type = e.getType();
                            int lim = Setting.MaxEntity.getOrDefault(type,Setting.DefMaxEntity);
                            int i = map.getOrDefault(type,0);
                            if (i > lim){
                                if (e instanceof Villager villager){
                                    //村民没法删除AI  那就给他凋零效果吧
                                    villager.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,100,1));
                                } else if (e instanceof Mob mob){
                                    MonitorUtil.clearEntityAI(mob);
                                } else {
                                    e.remove();
                                }
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

        lastChunkLoad = chunkLoad.getAndSet(0);
        lastGenerate = chunkGenerate.getAndSet(0);

        //通知协线程
        synchronized (this) {
            taskIsDone = true;
            this.notify();
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        chunkLoad.getAndIncrement();
        if (event.isNewChunk()) chunkGenerate.getAndIncrement();
    }

    public int getLastChunkLoad() {
        return lastChunkLoad;
    }

    public int getLastGenerate() {
        return lastGenerate;
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
        }

        public DenyRestart(long s,long time) {
            this.time = time;
            dny = s;
            last = System.currentTimeMillis();
            runTaskTimer(MemFree.plugin,20L,20L);
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
        final long interval = tickInterval * 1000;

        MFThread() {
            setName("MemFreeTimer");
            setDaemon(true);
        }

        @Override
        public void run() {
            warin = 0;
            while (isRun) {
                try{
                    synchronized (this) {
                        taskIsDone = false;
                        this.wait(interval * 2);
                    }
                    long now = System.currentTimeMillis(); //当前时间
                    //当服务器线程堵塞时
                    if (now - lastUpdateTime > Setting.shutdownHookWaitTime){
                        MemFree.logger.warning("线程堵塞超过" + CommonUtils.tanMintoh(Setting.shutdownHookWaitTime));
                        if (Setting.AutoRestart){
                            MemFree.logger.warning("强制终止进程");
                            Runtime.getRuntime().halt(9);
                        }
                    }
                    if (!taskIsDone){
                        warin++;
                        if (warin > Setting.Max_Warin){
                            if (Setting.AutoRestart) denyShwtdown();
                            else MemFree.logger.warning("服务器可能需要重启");
                        }
                        if (Setting.DEBUG) MemFree.logger.warning("卡顿警告");
                        tps = 0;
                        continue;
                    }
                    if (mem < Setting.minfree){
                        if (Setting.DEBUG) MemFree.logger.warning("内存警告");
                        warin++;
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
                        if (warin > Setting.Max_Warin){
                            if (Setting.AutoRestart) denyShwtdown();
                            else MemFree.logger.warning("服务器可能需要重启");
                        }
                    } else if (tps < Setting.mintps){
                        warin++;
                        if (warin > Setting.Max_Warin){
                            if (Setting.AutoRestart) denyShwtdown();
                            else MemFree.logger.warning("服务器可能需要重启");
                        }
                    } else if (warin > 0) warin--;

                    Runnable runnable;
                    //自动清理区块
                    runnable = Setting.autoClearUpWorld;
                    if (runnable != null) runnable.run();
                    //自动清理日志
                    runnable = Setting.autoCleanLog;
                    if (runnable != null) runnable.run();


                }catch (Throwable e){
                    MemFree.logger.info("计时器错误" + e.getMessage());
                    e.printStackTrace();
                }
            }
            logger.info("线程已关闭");
        }
    }
}
