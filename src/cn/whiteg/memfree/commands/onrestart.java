package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.*;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class onrestart extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        MFRunnable timer = MemFree.plugin.timer;

        long dny;
        if (args.length > 0){
            dny = CommonUtils.getTimeMintoh(args[0]);
        } else {
            dny = Setting.restartDeny;
        }

        long time;
        if (args.length > 1){
            time = CommonUtils.getTimeMintoh(args[1]);
        } else {
            time = 0;
        }

        if (dny <= 0){
            sender.sendMessage("参数有误");
            return false;
        }
        if (timer.stopRestart()){
            sender.sendMessage("已停止重启倒计时");
        } else {
            if (time > 0){
                timer.denyShwtdown(dny,time);
            } else {
                timer.denyShwtdown(dny);
            }
            sender.sendMessage("已设置重启倒计时:" + CommonUtils.tanMintoh(dny));
        }
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

    @Override
    public String getDescription() {
        return "设置重启倒计时:§7 [时间]";
    }
}
