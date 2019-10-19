package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.MemFree;
import cn.whiteg.mmocore.CommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class onrestart extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return true;
        }
        MemFree.plugin.timer.denyShwtdown();
        Bukkit.getServer().broadcastMessage("服务器将会在5秒后重启");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
