package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class reload extends HasCommandInterface {
    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        MemFree.plugin.reloadConfig();
        Setting.reload();
        MemFree.plugin.timer.stopTimer();
        MemFree.plugin.timer = new MFRunnable(MemFree.plugin);
        MemFree.plugin.timer.start();
        sender.sendMessage("§b重载完成");
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
