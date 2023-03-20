package cn.whiteg.memfree;

import cn.whiteg.memfree.runnable.AutoCleanLog;
import cn.whiteg.memfree.runnable.AutoClearUpWorld;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class Setting {
    public static Map<EntityType, Integer> MaxEntity = null;
    public static boolean DEBUG;
    public static boolean AutoRestart;
    public static int AllEntity;
    public static String f3info;
    public static long minfree;
    public static double mintps;
    public static int DefMaxEntity;
    public static int RunTick;
    public static short Max_Warin;
    public static boolean autoGc;
    public static long gcMinTick = 3600000L;
    public static int gcMinWarin = 3;
    public static boolean clearAI = false;
    public static int restartDeny = 5;
    public static double MaxEntity_Range;
    public static boolean updateMapFileDate;
    public static File WORLD_DATA_DIR;
    public static int entityExplosion = 0;
    public static long shutdownHookWaitTime;
    public static AutoClearUpWorld autoClearUpWorld;
    public static AutoCleanLog autoCleanLog;

    static public void reload() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MemFree.plugin.getDataFolder(),"config.yml"));
        DEBUG = config.getBoolean("debug");
        //TODO 检查配置文件版本号，提示或者更新配置文件
        AutoRestart = config.getBoolean("Auto_Restart",false);
        RunTick = config.getInt("runTick");
        f3info = config.getString("F3info");
        minfree = CommonUtils.toByteLength(config.getString("minfree","512M"));
        MemFree.logger.info("最小内存： " + CommonUtils.tanByte(minfree));
        Max_Warin = (short) config.getInt("MaxWaring",400);
        restartDeny = config.getInt("restartDeny",restartDeny) * 1000;
        updateMapFileDate = config.getBoolean("updateMapFileDate");
        entityExplosion = config.getInt("EntityExplosion",entityExplosion);
        shutdownHookWaitTime = CommonUtils.getTimeMintoh(config.getString("ShutdownHookWaitTime","2m"));

        ConfigurationSection sc;
        if (config.getBoolean("MaxEntity.Enable",false)){
            sc = config.getConfigurationSection("MaxEntity.Entitys");
            MaxEntity_Range = config.getDouble("MaxEntity.Range");
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
        sc = config.getConfigurationSection("AutoGC");
        if (sc != null){
            autoGc = sc.getBoolean("Enable",autoGc);
            gcMinTick = CommonUtils.getTimeMintoh(sc.getString("minTick","1h"));
            gcMinWarin = sc.getInt("minWarin",gcMinWarin);
        }


        WORLD_DATA_DIR = new File("world" + File.separator + "data");
        if (!WORLD_DATA_DIR.isDirectory()){
            MemFree.logger.warning("找不到文件夹" + WORLD_DATA_DIR);
        }

        sc = config.getConfigurationSection("SystemProperties");
        if (sc != null){
            final Properties properties = System.getProperties();
            for (String key : sc.getKeys(false)) {
                String value = sc.getString(key);
                if (value == null || value.isBlank()){
                    properties.remove(key);
                } else {
                    properties.setProperty(key,value);
                }
            }
        }


        //在服务器完全启动后加载的配置
        Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
            //初始化世界自动清理配置
            autoClearUpWorld = AutoClearUpWorld.deserialization(config.getConfigurationSection("AutoClearUpWorld"));
            //初始化自动清理日志
            autoCleanLog = AutoCleanLog.deserialization(config.getConfigurationSection("AutoCleanLog"));
        });
    }
}
