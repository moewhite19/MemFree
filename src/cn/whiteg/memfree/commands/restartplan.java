package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MFRunnable;
import cn.whiteg.memfree.MemFree;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class restartplan extends HasCommandInterface implements Listener {
    boolean enable = false;
    MFRunnable timer = MemFree.plugin.timer;
    String enableMessage = "§7服务器已启用更新计划，将会在最后一个玩家退出后重启";
    String disableMessage = "§7服务器放弃了更新计划";

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        enable = !enable;
        if (enable){
            MemFree.plugin.regListener(this);
            checkAndRestart();
        } else {
            MemFree.plugin.unregListener(this);
            timer.stopRestart();
        }
        notice();
        return true;
    }

    public void notice() {
        String msg = enable ? enableMessage : disableMessage;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public void checkAndRestart() {
        Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) timer.denyShwtdown();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        checkAndRestart();
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        timer.stopRestart();
        event.getPlayer().sendMessage(enableMessage);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "创建重启计划";
    }
}
