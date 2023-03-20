package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.Setting;
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
                ShellKit.runShell(sender,c);
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
        if(!Setting.DEBUG){
            return false;
        }
        return sender.hasPermission("memfree.shell");
    }

    @Override
    public String getDescription() {
        return "执行控制台指令";
    }
}
