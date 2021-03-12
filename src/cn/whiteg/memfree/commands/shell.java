package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.utils.ShellKit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class shell extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length > 0){
            StringBuilder sb = new StringBuilder(args[0]);
            int i = 1;
            while (i < args.length) {
                sb.append(" ").append(args[i]);
                i++;
            }
            String c = sb.toString().replace("&n","\n");
            sender.sendMessage("执行命令: " + c);
            try{
                sender.sendMessage(ShellKit.runShell(c).toString());
            }catch (Exception e){
                sender.sendMessage("执行命令出错: " + e.getMessage());
            }

            return true;
        }
        sender.sendMessage("未知指令");
        return false;
    }


    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("memfree.shell");
    }
}
