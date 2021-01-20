package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.MFRunnable;
import cn.whiteg.memfree.MemFree;
import cn.whiteg.memfree.Setting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class reload extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
            if (sender.hasPermission("MemFree.reload")){
                MemFree.plugin.reloadConfig();
                Setting.reload();
                MemFree.plugin.timer.stopTimer();
                MemFree.plugin.timer = new MFRunnable(MemFree.plugin);
                MemFree.plugin.timer.start();
                sender.sendMessage("§b重载完成");
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
