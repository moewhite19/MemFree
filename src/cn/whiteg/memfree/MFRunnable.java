package cn.whiteg.memfree;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.List;
import java.util.Timer;

import static org.apache.logging.log4j.LogManager.getLogger;

public class MFRunnable {
    private boolean isTimer;
    BukkitRunnable Runer;
    public void setTimer() {
        if (isTimer){return;}
        else {
            getLogger().info("启动计时器");
            isTimer = true;
            Runer = new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    //getLogger().info("运行次数" + i++);
                    long max = Runtime.getRuntime().maxMemory();
                    long use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    long free = max - use;
                    long minfree = MemFree.in().minfree;
                    //getLogger().info("最小内存" + minfree / 1024 / 1024);
                    //getLogger().info("剩余内存" + free / 1024 / 1024);
                    if (free < minfree) {
                        List<String> commands = MemFree.in().getConfig().getStringList("commands");
                        //解释一下 这里定义i为0 之后判断i是否小于List类型的command大小，是的话则i自加1
                        for (int i = 0; i < MemFree.in().getConfig().getStringList("commands").size(); i++) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),commands.get(i).replace("&","§"));
                        }
                        onCommand();
                    }
                }
            };
            Runer.runTaskTimer(MemFree.in(),200,20*MemFree.in().getConfig().getLong("runTick"));
        }
    }
    public void onCommand(){
        new BukkitRunnable() {
            @Override
            public void run() { List<String> commands = MemFree.in().getConfig().getStringList("onCommands");
                //解释一下 这里定义i为0 之后判断i是否小于List类型的command大小，是的话则i自加1
                for (int i = 0; i < MemFree.in().getConfig().getStringList("onCommands").size(); i++) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),commands.get(i));
                }
            }
        }.runTaskLater(MemFree.in(), 20*MemFree.in().getConfig().getLong("onTime"));
    }
    public void stopTimer(){
        Runer.cancel();
        Runer = null;
        isTimer = false;
        getLogger().info("已关闭计时器");
    }
    public boolean getTimer(){
        return isTimer;
    }
}
