package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;
import cn.whiteg.memfree.utils.MonitorUtil;
import cn.whiteg.memfree.utils.NMSUtils;
import cn.whiteg.memfree.utils.PluginUtil;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CommandManage extends CommandInterface {
    public Map<String, CommandInterface> commandMap = new HashMap<>();
    JavaPlugin plugin;
    String path;
    private String SERVER_VER;

    public CommandManage(JavaPlugin plugin) {
        this.plugin = plugin;
        path = plugin.getClass().getPackage().getName().replace('.','/') + "/commands";
        init();
    }

    public CommandManage(JavaPlugin plugin,String pack) {
        this.plugin = plugin;
        this.path = pack.replace('.','/');
        init();
    }

    //初始化
    private void init() {
        try{
            List<String> urls = PluginUtil.getUrls(plugin.getClass().getClassLoader(),false);
            for (String url : urls) {
                if (url.startsWith(path)){
                    int i = url.indexOf(".class");
                    if (i == -1) continue;
                    String path = url.replace('/','.').substring(0,i);
                    try{
                        Class<?> clazz = Class.forName(path);
                        if (CommandInterface.class.isAssignableFrom(clazz)){
                            CommandInterface ci = null;
                            //优先使用传入插件对象为参数的构造函数
                            for (Constructor<?> constructor : clazz.getConstructors()) {
                                Class<?>[] types = constructor.getParameterTypes();
                                if (types.length == 1 && plugin.getClass().isAssignableFrom(types[0])){
                                    ci = (CommandInterface) constructor.newInstance(plugin);
                                    break;
                                }
                            }
                            if (ci == null) ci = (CommandInterface) clazz.newInstance();
                            registerCommand(ci);
                        }
                    }catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e){
                        plugin.getLogger().warning("无法构建指令: " + path);
                        e.printStackTrace();
                    }
                }
            }
            for (Field field : SharedConstants.class.getDeclaredFields()) {
                if (field.getType().equals(WorldVersion.class)){
                    field.setAccessible(true);
                    try{
                        WorldVersion v = (WorldVersion) field.get(null);

                    }catch (IllegalAccessException e){
                        e.printStackTrace();
                    }
                    break;
                }
            }

//            SharedConstants.getGameVersion().getName();
            try{
                Field field = NMSUtils.getFieldFormType(SharedConstants.class,WorldVersion.class);
                field.setAccessible(true);
                WorldVersion ver = (WorldVersion) field.get(null);
                SERVER_VER = ver == null ? "NULL" : ver.getName();
            }catch (IllegalAccessException | NoSuchFieldException e){
                e.printStackTrace();
                SERVER_VER = e.getMessage();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        resizeMap();
    }

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 0){
            return onMain(sender);
        }
        CommandInterface subCommand = commandMap.get(args[0]);
        if (subCommand != null){
            if (args.length > 1){
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args,1,subArgs,0,subArgs.length);
                return subCommand.onCommand(sender,cmd,label,subArgs);
            } else {
                return subCommand.onCommand(sender,cmd,label,new String[]{});
            }
        } else {
            sender.sendMessage("无效指令");
        }
        return false;
    }

    public boolean onMain(CommandSender sender) {
        var timer = MemFree.plugin.timer;
        sender.sendMessage("§3[§bMemFree§3] §b当前服务器版本:§a" + SERVER_VER);
        sender.sendMessage("§b当前计时器" + (timer.isRun ? "§a开启" : "§7关闭") + " §b自动重启" + (Setting.AutoRestart ? "§a开启" : "§7关闭") + " §b预警值:§f" + MemFree.plugin.timer.warin);
        long max = Runtime.getRuntime().maxMemory();
        long use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        sender.sendMessage("§b内存使用率§3:§f " + String.format("%.2f%%",use / (double) max * 100) + String.format(" §b在线玩家§f%d/%d",Bukkit.getOnlinePlayers().size(),Bukkit.getMaxPlayers()));
        Server ser = Bukkit.getServer();
        String[] dtps = null;
        double[] o = MonitorUtil.getTps();
        if (o != null){
            dtps = new String[o.length];
            for (int i = 0; i < o.length; i++) {
                dtps[i] = String.format("%.2f",o[i]);
            }
        }
        sender.sendMessage("§bTPS§3:§r " + String.format("%.2f",timer.tps) + (dtps != null ? " §b平均:§f" + Arrays.toString(dtps) : " "));
        //获取线程数
        final int threadcount = Thread.currentThread().getThreadGroup().activeCount();
        sender.sendMessage("§b服务器已运行§3:§r " + CommonUtils.tanMintoh(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) + "  §3线程数:§f" + threadcount);
        sender.sendMessage("§b内存剩余§3:§f " + CommonUtils.tanByte(max) + " §7- §f" + CommonUtils.tanByte(use) + " §7= §f" + CommonUtils.tanByte(timer.mem) + "§7 " + "  §b已分配: §f" + CommonUtils.tanByte(Runtime.getRuntime().totalMemory()));
        final long dt = ser.getWorldContainer().getTotalSpace();
        final long du = ser.getWorldContainer().getUsableSpace();
        final long duse = dt - du;
        sender.sendMessage("§b磁盘剩余§3:§f " + CommonUtils.tanByte(dt) + " §7- §f" + CommonUtils.tanByte(duse) + " §7= §f" + CommonUtils.tanByte(du));
        sender.sendMessage("§b区块加载§7/§b生成数: §f" + timer.getLastChunkLoad() + "§7/§f" + timer.getLastGenerate());
        if (sender.hasPermission("memfree.moe")){
            for (World world : Bukkit.getWorlds()) {
                Chunk[] chunk = world.getLoadedChunks();
                int tiles = 0;
                for (Chunk c : chunk) {
                    tiles += c.getTileEntities().length;
                }

                sender.sendMessage(new StringBuilder().append("§b世界§f").append(world.getName())
                        .append("§b已加载区块§f").append(chunk.length)
                        .append(" §b所有实体§f").append(world.getEntities().size())
                        .append(" §b活动实体§f").append(world.getLivingEntities().size())
                        .append(" §bTiles§f").append(tiles).toString());
            }
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return getMatches(args[0].toLowerCase(),getCanUseCommands(sender));
        } else if (args.length > 1){
            CommandInterface subCommand = commandMap.get(args[0]);
            if (subCommand != null){
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args,1,subArgs,0,subArgs.length);
                return subCommand.onTabComplete(sender,cmd,label,subArgs);
            }
        }
        return null;
    }

    //获取玩家可用指令列表
    public List<String> getCanUseCommands(CommandSender sender) {
        List<String> list = new ArrayList<>(commandMap.size());
        commandMap.forEach((key,ci) -> {
            if (ci.canUseCommand(sender)) list.add(key);
        });
        return list;
    }

    public Map<String, CommandInterface> getCommandMap() {
        return commandMap;
    }

    //注册指令
    public void registerCommand(CommandInterface ci) {
        String name = ci.getName();
        commandMap.put(name,ci);
        PluginCommand pc = plugin.getCommand(name);
        if (pc != null){
            pc.setExecutor(ci);
            pc.setTabCompleter(ci);
            final String description = ci.getDescription();
            if (!description.isBlank()){
                pc.setDescription(description);
            }
        }
    }

    //构建完毕后固定map大小
    public void resizeMap() {
        commandMap = new HashMap<>(commandMap);
    }

    //设置指令执行器
    public void setExecutor(String name) {
        PluginCommand pc = plugin.getCommand(name);
        if (pc != null){
            pc.setExecutor(this);
            pc.setTabCompleter(this);
        }
    }

    public void setExecutor() {
        setExecutor(plugin.getName().toLowerCase());
    }
}
