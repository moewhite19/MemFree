package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class gc extends CommandInterface {
    final String p = "[System.gc()]";

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return true;
        }
        Bukkit.broadcastMessage(p + "服务器开始强制回收内存,可能会有短暂卡顿");
        long n = System.currentTimeMillis();
        final Runtime r = Runtime.getRuntime();
        final long m = r.freeMemory();
        System.gc();
        long now_m = r.freeMemory() - m;
        Bukkit.broadcastMessage(p + "内存回收完成,回收了" + now_m / 1024 / 1024 + "MB内存 耗时" + (System.currentTimeMillis() - n) + "ms");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
