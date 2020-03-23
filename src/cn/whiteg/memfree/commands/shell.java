package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.utils.ShellKit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.List;

public class shell extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.shell")){
            sender.sendMessage("没有权限");
            return true;
        }
        if (args.length > 1){
            StringBuilder sb = new StringBuilder(args[1]);
            int i = 2;
            while (i < args.length){
                sb.append(" ").append(args[i]);
                i++;
            }
            String c = sb.toString().replace("&n" , "\n");
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
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
