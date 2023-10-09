package cn.whiteg.memfree.runnable;

import cn.whiteg.memfree.MemFree;
import cn.whiteg.memfree.commands.clearchunk;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record AutoClearUpWorld(int clearUpNumber,long minFreeSpace,Map<File, Long> worlds) implements Runnable {

    public static AutoClearUpWorld deserialization(ConfigurationSection sc) {
        if (sc == null || !sc.getBoolean("Enable",false)) return null;
        var worldClearUpNumber = sc.getInt("ClearNumber",0);
        var autoClearMinFreeSpace = CommonUtils.toByteLength(sc.getString("MinFreeSpace",""));
        MemFree.logger.info("自动清理区块筏值: " + CommonUtils.tanByte(autoClearMinFreeSpace));
        if (worldClearUpNumber <= 0 || autoClearMinFreeSpace <= 0) return null;

        ConfigurationSection worldCS = sc.getConfigurationSection("Worlds");
        if (worldCS == null) return null;
        Set<String> worldKeys = worldCS.getKeys(false);
        if (worldKeys.isEmpty()) return null;
        var map = new HashMap<File, Long>(worldKeys.size());

        for (String name : worldKeys) {
            World world = Bukkit.getWorld(name);
            if (world != null){
                long time = (long) (worldCS.getDouble(name,0D) * 86400000L);
                map.put(clearchunk.getWorldRegionDir(world),time);
                MemFree.logger.info(name + " 当区块最后访问时间距离现在小于 " + CommonUtils.tanMintoh(time) + " 时跳过");
            } else {
                MemFree.logger.info("没有找到世界: " + name);
            }
        }

        if (map.isEmpty()) return null;

        return new AutoClearUpWorld(worldClearUpNumber,autoClearMinFreeSpace,map);
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long free = Bukkit.getServer().getWorldContainer().getFreeSpace();
        if (free < minFreeSpace()){
            MemFree.logger.warning("磁盘剩余空间 " + CommonUtils.tanByte(free) + "开始自动清理世界");
            File[] regionCache = new File[clearUpNumber];
            Long[] modifiedCache = new Long[clearUpNumber];
            worlds().forEach((dir,recent) -> {
                if (!dir.isDirectory()) return;
                var files = dir.listFiles();
                if (files == null) return;
                for (File region : files) {
                    var modif = region.lastModified() + recent;
                    for (int i = 0; i < regionCache.length; i++) {
                        if (modif < now && (regionCache[i] == null || modif < modifiedCache[i])){
                            regionCache[i] = region;
                            modifiedCache[i] = modif;
                            break;
                        }
                    }
                }
            });
            int done = 0;
            long size = 0;
            int pdone = 0;
            int psize = 0;
            for (File regionFile : regionCache) {
                if (regionFile == null) break; //到null说明后面不会有数据了
                if (regionFile.exists()){
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
                        MemFree.logger.warning("清理失败" + name);
                        e.printStackTrace();
                    }
                }
            }
            MemFree.logger.warning(size > 0 ? ("已清理 " + done + " 个区域文件(" + CommonUtils.tanByte(size) + ")和" + pdone + "个poi文件(" + (CommonUtils.tanByte(psize)) + ")") : "没有清理掉任何文件");
        }
    }
}
