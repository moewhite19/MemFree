package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.Setting;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class sleep extends HasCommandInterface {
    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        long m;
        if (args.length > 0){
            try{
                m = Long.parseLong(args[0]);
            }catch (NumberFormatException e){
                sender.sendMessage("参数有误");
                return false;
            }
        } else {
            sender.sendMessage("请加上参数[毫秒]");
            return false;
        }
        try{
            Thread.sleep(m);
        }catch (InterruptedException e){
            sender.sendMessage("执行出错" + e);
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test") && Setting.DEBUG;
    }

    @Override
    public String getDescription() {
        return "让服务器线程休眠，模拟线程堵塞";
    }
}
