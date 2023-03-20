package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class pdistance extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 0){
            sender.sendMessage("没有参数");
            return false;
        }
        final Player p = Bukkit.getPlayer(args[0]);
        if (p == null){
            sender.sendMessage("找不到玩家");
            return false;
        }
        if (args.length == 1){
            int ints = MonitorUtil.getDistance(p);
            sender.sendMessage("玩家" + p.getName() + "视距为" + ints);

        } else if (args.length == 2){
            final int vd;
            try{
                vd = Integer.parseInt(args[1]);
                //sender.sendMessage("已修改视距为" + vd + "实体可见范围为" + ed);
            }catch (Exception e){
                //e.printStackTrace();
                sender.sendMessage("参数无效");
                return true;
            }
            MonitorUtil.setDistance(p,vd);
            sender.sendMessage("玩家" + p.getName() + "视距已更新");

        } else {
            sender.sendMessage("无效参数");
        }
        return true;
    }


    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "查看或修改玩家视距";
    }
}
