package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
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
import oshi.util.FileUtil;

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

    @SuppressWarnings("ConstantConditions")
    public static int cleanPoiFile(File regionDir,File poiDir) {
        int done = 0;
        if (poiDir.isDirectory()){
            for (File poiFile : poiDir.listFiles()) {
                String name = poiFile.getName();
                File nf = new File(regionDir,name);
                if (!nf.exists() && poiFile.delete()){
                    done++;
                }
            }
        }
        return done;
    }

    @SuppressWarnings("ConstantConditions")
    public static int cleanMapTitle(World world) {
        final File titlesDir = new File("plugins" + File.separatorChar + "dynmap" + File.separatorChar + "web" + File.separatorChar + "tiles" + File.separatorChar + world.getName());
        File regionDir = getWorldRegionDir(world);
        if (!regionDir.isDirectory()) return 0;
        int done = 0;
        if (titlesDir.isDirectory()){
            for (File hashFile : titlesDir.listFiles()) {
                if (!hashFile.isFile()) continue;
                String name = hashFile.getName();
                //去格式名
                int index = name.indexOf('.');
                if (index == -1){
                    continue;
                }
                name = name.substring(0,index);
                String[] args = name.split("_"); //分组
                if (args.length != 3){
                    MemFree.logger.warning("非地图文件: " + name);
                    continue;
                }

                //格式化获取坐标
                int x, z;
                try{
                    x = Integer.parseInt(args[1]);
                    z = Integer.parseInt(args[2]);
                }catch (NumberFormatException e){
                    MemFree.logger.warning(e.getMessage());
                    continue;
                }


                //最终对应的region文件名
                name = "r." + x + '.' + z + ".mca";
                File nf = new File(regionDir,name);
//                System.out.println("检查文件" + nf + ": " + (nf.exists() ? "存在" : "不存在"));
                if (!nf.exists()){
                    final File layerFolder = new File(titlesDir,args[0]);
                    final File mapRegion = new File(layerFolder,String.join("_",String.valueOf(x),String.valueOf(z)));
                    if (mapRegion.isDirectory()){
                        for (File file : mapRegion.listFiles()) {
                            file.delete();
                        }
                        if (!mapRegion.delete()){
                            MemFree.logger.warning("无法删除文件夹: " + mapRegion);
                            continue;
                        }
                    }


                    if (!hashFile.delete()){
                        MemFree.logger.warning("无法删除文件: " + mapRegion);
                        continue;
                    }
                    done++;
                }
            }
        }
        return done;
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
                for (File regionFile : fileList) {
                    if (regionFile != null && regionFile.exists()){
                        String name = regionFile.getName();
                        try{
                            if (!regionFile.delete()) continue;
                            done++;
                            size += regionFile.length();
                            File dir = new File(regionFile.getParentFile().getParentFile(),"poi");
                            if (dir.isDirectory()){
                                File poi = new File(dir,name);
                                if (poi.exists() && poi.delete()){
                                    psize += poi.length();
                                    pdone++;
                                }
                            }

                            dir = new File(regionFile.getParentFile().getParentFile(),"entities");
                            if (dir.isDirectory()){
                                File poi = new File(dir,name);
                                if (poi.exists() && poi.delete()){
                                    psize += poi.length();
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
                    done += cleanPoiFile(regionDir,dir);
                    dir = new File(regionDir.getParentFile(),"entities");
                    done += cleanPoiFile(regionDir,dir);
                }
                sender.sendMessage("清理了 " + done + " 个无效poi文件");
            } else if (sta.equals("dynmap")){
                int done = 0;
                for (World world : Bukkit.getWorlds()) {
                    done += cleanMapTitle(world);
                }
                sender.sendMessage("清理了 " + done + " 个无效Map文件");
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
            //noinspection deprecation
            BaseComponent[] cb = new ComponentBuilder("找到 " + fileList.size() + " 个区域文件, 在输入一次指令或点击")
                    .color(ChatColor.AQUA)
                    .append(configCmd)
                    .color(ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,configCmd))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("点我确认删除").create()))
                    .create();
            sender.spigot().sendMessage(cb);
        } else {
            sender.sendMessage(getDescription());
        }
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
                worlds.addAll(Arrays.asList("poi","confirm","dynmap"));
            }
            return getMatches(worlds,args);
        }
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "清理过时区块:§7 <世界名> <几天前>";
    }
}
