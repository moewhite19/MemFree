package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class help extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        sender.sendMessage("§3[§b" + MemFree.plugin.getDescription().getFullName() + "§3]");
        for (Map.Entry<String, CommandInterface> entry : MemFree.plugin.mainCmd.commandMap.entrySet()) {
            CommandInterface ci = entry.getValue();
            if (ci.canUseCommand(sender)) sender.sendMessage("§a" + ci.getName() + "§f:§b " + ci.getDescription());
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "显示帮助";
    }
}
