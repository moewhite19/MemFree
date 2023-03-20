package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

;

public class distance extends HasCommandInterface {

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 0){
            for (World world : Bukkit.getWorlds()) {
                int ints = MonitorUtil.getDistance(world);
                sender.sendMessage(world.getName() + "§b区块可见距离为§r" + ints);
            }
        } else if (args.length == 1){
            int vd;
            try{
                vd = Integer.parseInt(args[0]);
                sender.sendMessage("所有世界视距已更新");
            }catch (Exception e){
                //e.printStackTrace();
                sender.sendMessage("参数无效");
                return true;
            }
            MonitorUtil.setDistance(vd);
        } else if (args.length == 2){
            World world = Bukkit.getWorld(args[0]);
            int vd;
            if (world == null){
                sender.sendMessage("找不到世界");
                return true;
            }
            try{
                vd = Integer.parseInt(args[1]);
                //sender.sendMessage("已修改视距为" + vd + "实体可见范围为" + ed);
            }catch (Exception e){
                //e.printStackTrace();
                sender.sendMessage("参数无效");
                return true;
            }
            MonitorUtil.setDistance(world,vd);
            sender.sendMessage("世界视距已更新");

        } else {
            sender.sendMessage("无效参数");
        }
        return true;
    }


    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return getMatches(worlds,args);
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "设置或者查看世界视距";
    }
}
