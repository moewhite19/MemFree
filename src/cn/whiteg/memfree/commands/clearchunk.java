package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.utils.CommonUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class clearchunk extends HasCommandInterface {
    public static List<File> fileList = null;

    public static List<File> checkWorld(World world,double day) {
        File div = getWorldRegionDir(world);
        File[] files = div.listFiles();
        ArrayList<File> list = new ArrayList<>();
        for (File f : files) {
            if (!f.isFile()) continue;
            long now = System.currentTimeMillis();
            long modified = f.lastModified();
            long differenceValue = now - modified;
            long maxDifference = Math.round(day * 86400000);
//            double d = Math.round((double) differenceValue / 86400000);
            if (differenceValue >= maxDifference){
                list.add(f);
            }
        }
        return list;
    }

//    public static void clearRegion(List<File> list) {
//
//    }


    public static File getWorldRegionDir(World world) {
        if (world.getEnvironment() == World.Environment.NORMAL){
            return new File(world.getName() + File.separator + "region");
        } else if (world.getEnvironment() == World.Environment.NETHER){
            return new File(world.getName() + File.separator + "DIM-1" + File.separator + "region");
        }
        return new File(world.getName() + File.separator + "DIM1" + File.separator + "region");
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            String sta = args[0];
            if (fileList != null && sta.equals("confirm")){
                int done = 0;
                long size = 0;
                int pdone = 0;
                int psize = 0;
                for (File f : fileList) {
                    if (f.exists()){
                        String name = f.getName();
                        try{
                            size += f.length();
                            f.delete();
                            done++;
                            File dir = new File(f.getParentFile().getParentFile(),"poi");
                            if (dir.isDirectory()){
                                File poi = new File(dir,name);
                                size += poi.length();
                                if (poi.exists()){
                                    psize += poi.length();
                                    poi.delete();
                                    pdone++;
                                }
                            }

                            dir = new File(f.getParentFile().getParentFile(),"entities");
                            if (dir.isDirectory()){
                                File poi = new File(dir,name);
                                size += poi.length();
                                if (poi.exists()){
                                    psize += poi.length();
                                    poi.delete();
                                    pdone++;
                                }
                            }

                        }catch (Exception e){
                            sender.sendMessage("清理失败" + name);
                            e.printStackTrace();
                        }
                    }
                }
                sender.sendMessage("已清理 " + done + " 个区域文件(" + CommonUtils.tanByte(size) + ")和" + pdone + "个poi文件(" + (CommonUtils.tanByte(psize)) + ")");
            } else if (sta.equals("poi")){
                int done = 0;
                for (World world : Bukkit.getWorlds()) {
                    File regionDir = getWorldRegionDir(world);
                    if (!regionDir.isDirectory()) continue;
                    File dir = new File(regionDir.getParentFile(),"poi");
                    done = clean(done,regionDir,dir);
                    dir = new File(regionDir.getParentFile(),"entities");
                    done = clean(done,regionDir,dir);
                }
                sender.sendMessage("清理了 " + done + " 个无效poi文件");
            } else {
                sender.sendMessage("无效参数");
                return false;
            }
        } else if (args.length == 2){
            sender.sendMessage("清理区块");
            World world = Bukkit.getWorld(args[0]);
            if (world == null){
                sender.sendMessage("世界不存在");
                return false;
            }
            double day;
            try{
                day = Double.parseDouble(args[1]);
            }catch (NumberFormatException e){
                sender.sendMessage("无效数值");
                return false;
            }
            fileList = checkWorld(world,day);
            if (fileList.isEmpty()){
                sender.sendMessage("没有找到需要清理的区块文件");
                fileList = null;
                return false;
            }
            String configCmd = "/mf " + getName() + " confirm";
            BaseComponent[] cb = new ComponentBuilder("找到 " + fileList.size() + " 个区域文件, 在输入一次指令或点击")
                    .color(ChatColor.AQUA)
                    .append(configCmd)
                    .color(ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,configCmd))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("点我确认删除").create()))
                    .create();
            sender.spigot().sendMessage(cb);
//            sender.sendMessage("总共" + fileList.size() + "个区域文件, 在输入一次指令或点击§a/mf " + args[0] + " confirm §r确认删除");
        }
        return true;
    }

    private int clean(int done,File regionDir,File dir) {
        if (dir.isDirectory()){
            for (File file : dir.listFiles()) {
                String name = file.getName();
                File nf = new File(regionDir,name);
                if (!nf.exists()){
                    file.delete();
                    done++;
                }
            }
        }
        return done;
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
                worlds.addAll(Arrays.asList("poi","confirm"));
            }
            return getMatches(worlds,args);
        }
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }
}
