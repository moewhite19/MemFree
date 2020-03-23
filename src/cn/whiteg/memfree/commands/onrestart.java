package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.MFRunnable;
import cn.whiteg.memfree.MemFree;
import cn.whiteg.memfree.Setting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class onrestart extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return false;
        }
        MFRunnable t = MemFree.plugin.timer;
        int dny = Setting.restartDeny;
        if (args.length > 1){
            try{
                dny = Integer.parseInt(args[1]);
            }catch (NumberFormatException ignored){
            }
        }
        if (t.stopRestart()){
            sender.sendMessage("已停止重启倒计时");
        } else {
            t.denyShwtdown(dny);
            if (!(sender instanceof Player)){
                Bukkit.getServer().broadcastMessage("服务器将会在" + dny + "秒后重启");
            }
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
