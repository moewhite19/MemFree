package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class toggle extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        if (sender.hasPermission("memfree.toggle")){
            if (MemFree.plugin.timer.isRun){
                MemFree.plugin.timer.stopTimer();
            } else {
                MemFree.plugin.timer.start();
            }
            return true;
        } else {
            sender.sendMessage("§b阁下没有权限使用这个指令");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
