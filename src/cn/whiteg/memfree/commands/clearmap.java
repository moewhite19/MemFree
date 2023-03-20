package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.Setting;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class clearmap extends HasCommandInterface {
    public static List<File> fileList = null;

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            String sta = args[0];
            if (sta.equals("confirm")){
                if (fileList == null){
                    return false;
                }
                int done = 0;
                for (File f : fileList) {
                    if (f.exists()){
                        String name = f.getName();
                        try{
                            f.delete();
                            done++;
                        }catch (Exception e){
                            sender.sendMessage("清理失败" + name);
                            e.printStackTrace();
                        }
                    }
                }
                sender.sendMessage("已清理 " + done + " 个地图文件");
            } else {
                sender.sendMessage("清理地图文件");
                double day;
                try{
                    day = Double.parseDouble(args[0]);
                }catch (NumberFormatException e){
                    sender.sendMessage("无效数值");
                    return false;
                }
                fileList = new ArrayList<>();
                for (File f : Setting.WORLD_DATA_DIR.listFiles()) {
                    if (f.isDirectory() || !f.getName().startsWith("map_")) continue;
                    long now = System.currentTimeMillis();
                    long modified = f.lastModified();
                    long differenceValue = now - modified;
                    long maxDifference = Math.round(day * 86400000);
//            double d = Math.round((double) differenceValue / 86400000);
                    if (differenceValue >= maxDifference){
                        fileList.add(f);
                    }
                }
                if (fileList.isEmpty()){
                    sender.sendMessage("没有找到需要清理的区块文件");
                    fileList = null;
                    return false;
                }
                String configCmd = "/mf " + getName() + " confirm";
                BaseComponent[] cb = new ComponentBuilder("找到 " + fileList.size() + " 个地图文件, 在输入一次指令或点击")
                        .color(ChatColor.AQUA)
                        .append(configCmd)
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,configCmd))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("点我确认删除").create()))
                        .create();
                sender.spigot().sendMessage(cb);
            }
        }
        return true;
    }
    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "清理地图";
    }
}
