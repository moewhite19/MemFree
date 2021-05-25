package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class white extends HasCommandInterface {
    final String p = "[System.gc()]";

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        Bukkit.broadcastMessage(p + "服务器开始强制回收内存,可能会有短暂卡顿");
        while (true) {
            if (false) break;

        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }
}
