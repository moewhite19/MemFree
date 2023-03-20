package cn.whiteg.memfree.Listener;

import cn.whiteg.memfree.MFRunnable;
import cn.whiteg.memfree.MemFree;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerListener implements Listener {

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void playerquit(PlayerQuitEvent paramInventoryClickEvent) {
//        //Player who = paramInventoryClickEvent.getPlayer();
//        //
//    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
//        CraftPlayer cp = (CraftPlayer) event.getPlayer();
//        EntityPlayer ep = cp.getHandle();
//        PlayerConnection pc = ep.playerConnection;
//        new BukkitRunnable() {
//            /**
//             * When an object implementing interface <code>Runnable</code> is used
//             * to create a thread, starting the thread causes the object's
//             * <code>run</code> method to be called in that separately executing
//             * thread.
//             * <p>
//             * The general contract of the method <code>run</code> is that it may
//             * take any action whatsoever.
//             *
//             * @see Thread#run()
//             */
//            @Override
//            public void run() {
//                long w = pc.networkManager.channel.bytesBeforeWritable();
//                long u = pc.networkManager.channel.bytesBeforeUnwritable();
//                cp.sendActionBar("写入:" + w + " 上传: " + u);
//
//            }
//        }.runTaskTimer(MemFree.plugin,1,1);
        BossBar bar = getBar();
        if (bar != null) bar.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        BossBar bar = getBar();
        if (bar != null) bar.removePlayer(event.getPlayer());
    }

    public BossBar getBar() {
        MFRunnable.DenyRestart d = MemFree.plugin.timer.getDenyTask();
        if (d != null) return d.getBar();
        return null;
    }

}

