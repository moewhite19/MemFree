package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class toggle extends HasCommandInterface {

    @Override
    public boolean executor(CommandSender sender,Command cmd,String label,String[] args) {
        if (MemFree.plugin.timer.isRun){
            MemFree.plugin.timer.stopTimer();
        } else {
            MemFree.plugin.timer.start();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("memfree.toggle");
    }
}
