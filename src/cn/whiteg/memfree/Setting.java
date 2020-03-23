package cn.whiteg.memfree;

import cn.whiteg.memfree.commands.clearchunk;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.*;

public class Setting {
    public static Map<EntityType, Integer> MaxEntity = null;
    public static boolean DEBUG;
    public static boolean AutoRestart;
    public static int AllEntity;
    public static double MaxSpawn_Range;
    public static String f3info;
    public static long minfree;
    public static double mintps;
    public static int DefMaxEntity;
    public static int RunTick;
    public static short Max_Warin;
    public static int limElytra;
    public static boolean autoGc = false;
    public static long gcMinTick = 3600000L;
    public static int gcMinWarin = 3;
    public static boolean clearAI = false;
    public static int restartDeny = 5;
    public static boolean enableAutoClearUpWorld = false;
    public static int worldClearUpNumber = 0;
    public static long autoClearMinFreeSpace = 0;
    public static File[] autoClearUpWorlds = null;
    public static Long[] autoClearUpWhordsOffset = null;

    static public void reload() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MemFree.plugin.getDataFolder(),"config.yml"));
        DEBUG = config.getBoolean("debug");
        AutoRestart = config.getBoolean("Auto_Restart",false);
        RunTick = config.getInt("runTick");
        f3info = config.getString("F3info");
        minfree = config.getLong("minfree") * 1024 * 1024;
        Max_Warin = (short) config.getInt("MaxWaring",400);
        limElytra = config.getInt("limElytra",0);
        restartDeny = config.getInt("restartDeny",restartDeny);
        ConfigurationSection sc;
        if (config.getBoolean("MaxEntity.Enable",false)){
            sc = config.getConfigurationSection("MaxEntity.Entitys");
            MaxSpawn_Range = config.getDouble("MaxEntity.Range");
            AllEntity = config.getInt("MaxEntity.MaxAllEntity");
            DefMaxEntity = config.getInt("MaxEntity.DefMaxEntity");
            clearAI = config.getBoolean("MaxEntity.ClearAI",clearAI);
            MaxEntity = new EnumMap<>(EntityType.class);
            if (sc != null) for (String s : sc.getKeys(false)) {
                try{
                    int num = sc.getInt(s);
                    MaxEntity.put(EntityType.valueOf(s),num);
                    MemFree.logger.info("实体" + s + "数量限制为" + num);
                }catch (IllegalArgumentException e){
                    MemFree.logger.warning("无效实体" + s);
                }
            }
        } else {
            MaxEntity = null;
        }
        sc = config.getConfigurationSection("AutoGc");
        if (sc != null){
            autoGc = sc.getBoolean("Enable",autoGc);
            long tick = sc.getInt("minTick",0);
            if (tick > 0) gcMinTick = tick * 1000;
            gcMinWarin = sc.getInt("minWarin",gcMinWarin);
        }

        //初始化世界自动清理配置
        sc = config.getConfigurationSection("AutoClearUpWorld");
        if (sc != null){
            enableAutoClearUpWorld = sc.getBoolean("Enable",enableAutoClearUpWorld);
            if (enableAutoClearUpWorld){
                worldClearUpNumber = sc.getInt("ClearNumber",worldClearUpNumber);
                autoClearMinFreeSpace = sc.getLong("MinFreeSpace",autoClearMinFreeSpace);

                if (worldClearUpNumber <= 0 || autoClearMinFreeSpace <= 0){
                    enableAutoClearUpWorld = false;
                } else {
                    autoClearMinFreeSpace = autoClearMinFreeSpace * 1024 * 1024;
                    ConfigurationSection worldCS = sc.getConfigurationSection("Worlds");
                    if (worldCS != null){
                        Set<String> worldKeys = worldCS.getKeys(false);
                        if (!worldKeys.isEmpty()){
                            List<File> worlds = new ArrayList<>(worldKeys.size());
                            List<Long> offset = new ArrayList<>(worldKeys.size());
                            Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
                                for (String name : worldKeys) {
                                    World world = Bukkit.getWorld(name);
                                    if (world != null){
                                        int v = worldCS.getInt(name,0);
                                        worlds.add(clearchunk.getWorldRegionDir(world));
                                        offset.add((long) (v * 86400000));
                                        MemFree.logger.info("自动清理世界" + name + "补偿天数: " + v);
                                    } else {
                                        MemFree.logger.info("无效的自动清理世界: " + name);
                                    }
                                }
                                if (worlds.isEmpty()){
                                    enableAutoClearUpWorld = false;
                                    return;
                                }
                                autoClearUpWorlds = worlds.toArray(new File[0]);
                                autoClearUpWhordsOffset = offset.toArray(new Long[0]);
                            });
                        } else {
                            enableAutoClearUpWorld = false;
                        }
                    } else {
                        enableAutoClearUpWorld = false;
                    }
                }
            }
        }

    }

}
