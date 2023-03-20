package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class toggle extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (MemFree.plugin.timer.isRun){
            MemFree.plugin.timer.stopTimer();
        } else {
            MemFree.plugin.timer.start();
        }
        sender.sendMessage(" §b已" + (MemFree.plugin.timer.isRun ? "§a启动" : "§c关闭") + "§b计时器");
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

    @Override
    public String getDescription() {
        return "开关计时器(插件所有功能)";
    }
}
