package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.CommandInterface;
import cn.whiteg.memfree.MFRunnable;
import cn.whiteg.memfree.MemFree;
import cn.whiteg.memfree.Setting;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class onrestart extends CommandInterface {

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return false;
        }
        MFRunnable t = MemFree.plugin.timer;

        long dny;
        if (args.length > 1){
            dny = CommonUtils.getTimeMintoh(args[1]);
        } else {
            dny = Setting.restartDeny;
        }

        long time;
        if (args.length > 2){
            time = CommonUtils.getTimeMintoh(args[2]);
        } else {
            time = 0;
        }

        if (dny <= 0){
            sender.sendMessage("参数有误");
            return false;
        }
        if (t.stopRestart()){
            sender.sendMessage("已停止重启倒计时");
        } else {
            if (time > 0){
                t.denyShwtdown(dny,time);
            } else {
                t.denyShwtdown(dny);
            }
            sender.sendMessage("已设置重启倒计时:" + CommonUtils.tanMintoh(dny));
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
