package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class stop extends CommandInterface {
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
        }
        Thread t = MemFree.plugin.timer.getMainThread();
        t.stop();
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
