package cn.whiteg.memfree;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class Setting {
    public static Map<EntityType, Integer> MaxEntity = new EnumMap<>(EntityType.class);
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

    static public void reload() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(MemFree.plugin.getDataFolder(),"config.yml"));
        DEBUG = config.getBoolean("debug");
        AutoRestart = config.getBoolean("Auto_Restart",false);
        RunTick = config.getInt("runTick");
        f3info = config.getString("F3info");
        minfree = config.getLong("minfree") * 1024 * 1024;
        MaxSpawn_Range = config.getDouble("MaxSpawn.Range");
        AllEntity = config.getInt("MaxSpawn.MaxAllEntity");
        DefMaxEntity = config.getInt("MaxSpawn.DefMaxEntity");
        Max_Warin = (short) config.getInt("MaxWaring",400);
        limElytra = config.getInt("limElytra",0);
        ConfigurationSection sc = config.getConfigurationSection("MaxSpawn.MaxEntity");

        if (sc != null) for (String s : sc.getKeys(false)) {
            try{
                int num = sc.getInt(s);
                MaxEntity.put(EntityType.valueOf(s),num);
                MemFree.logger.info("实体" + s + "数量限制为" + num);
            }catch (IllegalArgumentException e){
                MemFree.logger.warning("无效实体" + s);
            }
        }
    }
}
