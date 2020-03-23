package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;
import cn.whiteg.memfree.utils.MonitorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandManage extends CommandInterface {
    final public List<String> allCmd = Arrays.asList("reload","clearchunk","clearmap","toggle","gc","onrestart","shell","monitor","distance","pdistance","test","stop");
    final public Map<String, CommandInterface> commandMap = new HashMap<>(allCmd.size());
    final public SubCommand subCommand = new SubCommand(this);

    public CommandManage() {
        for (int i = 0; i < allCmd.size(); i++) {
            String cmd = allCmd.get(i);
            try{
                Class c = Class.forName("cn.whiteg.memfree.commands." + cmd);
                CommandInterface ci = (CommandInterface) c.newInstance();
                regCommand(cmd,ci);
                PluginCommand pc = MemFree.plugin.getCommand(cmd);
                if(pc != null){
                    pc.setExecutor(subCommand);
                    pc.setTabCompleter(subCommand);
                }
            }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
                MemFree.logger.warning("没有注册指令" + cmd + ": " + e.getMessage());
            }
        }
    }

    public static List<String> getMatches(List<String> list,String value) {
        return getMatches(list,value);
    }

    public static List<String> PlayersList(String arg) {
        List<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) players.add(p.getName());
        return getMatches(arg,players);
    }

    public static List<String> PlayersList(String[] arg) {
        return PlayersList(arg[arg.length - 1]);
    }

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 0){
            sta(sender);
            return true;
        }
        CommandInterface c = commandMap.get(args[0]);
        if (c != null){
            return c.onCommand(sender,cmd,label,args);
        } else {
            sender.sendMessage("无效指令");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length > 1){
            List ls = null;
            if (commandMap.containsKey(args[0])) ls = commandMap.get(args[0]).onTabComplete(sender,cmd,label,args);
            if (ls != null){
                return getMatches(args[args.length - 1],ls);
            }
        }
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
        if (args.length == 1){
            return getMatches(args[0],allCmd);
        }
        return null;
    }

    public void sta(CommandSender sender) {
        sender.sendMessage("§3[§bMemFree§3]");
        sender.sendMessage("§b当前计时器" + (MemFree.plugin.timer.isTimer ? "§a已启用" : "§7未启用") + " §b自动重启" + (Setting.AutoRestart ? "§a已启用" : "§7未启用") + " §b危险程度:§b" + MemFree.plugin.timer.warin);
        long max = Runtime.getRuntime().maxMemory();
        long use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        sender.sendMessage("§b内存使用率§3:§f " + String.format("%.2f",use / (double) max * 100) + "%");
        Server ser = Bukkit.getServer();
        String[] dtps = null;
        double[] o = MonitorUtil.getTps();
        if (o != null){
            dtps = new String[o.length];
            for (int i = 0; i < o.length; i++) {
                dtps[i] = String.format("%.2f",o[i]);
            }
        }
        sender.sendMessage("§bTPS§3:§r " + String.format("%.2f",MemFree.plugin.timer.tps) + (dtps != null ? " §b平均:§f" + Arrays.toString(dtps) : " "));
        //获取线程数
        final int threadcount = Thread.currentThread().getThreadGroup().activeCount();
        sender.sendMessage("§b服务器已运行§3:§r " + CommonUtils.tanMintoh(Math.round((double) (System.currentTimeMillis() - MemFree.runtime) / 1000 / 60)) + "  §3线程数:§f" + threadcount);
        sender.sendMessage("§b内存使用§3:§f " + max / 1024 / 1024 + "MB§7-§f" + use / 1024 / 1024 + "MB §7= §f" + MemFree.plugin.timer.Mem / 1024 / 1024 + "§7MB " + "  §b已启用: §f" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "§7MB");
        final long dt = ser.getWorldContainer().getTotalSpace();
        final long du = ser.getWorldContainer().getUsableSpace();
        final long duse = dt - du;
        sender.sendMessage("§b磁盘空间§3:§f " + CommonUtils.tanByte(dt) + " §7- §f" + CommonUtils.tanByte(duse) + " §7= §f" + CommonUtils.tanByte(du));
        if (sender.hasPermission("memfree.moe")){
            for (World world : Bukkit.getWorlds()) {
                Chunk[] chunk = world.getLoadedChunks();
                int titles = 0;
                for (Chunk c : chunk) {
                    titles += c.getTileEntities().length;
                }
                sender.sendMessage(new StringBuilder().append("§b世界§f").append(world.getName()).append("§b已加载区块§f").append(chunk.length).append(" §b所有实体§f").append(world.getEntities().size()).append(" §b活动实体§f").append(world.getLivingEntities().size()).append(" §bTitles§f").append(titles).toString());
            }
        }
    }

    public void regCommand(String var1,CommandInterface cmd) {
        commandMap.put(var1,cmd);
    }


    public static class SubCommand extends CommandInterface {
        private final CommandManage commandManage;

        SubCommand(CommandManage commandManage) {
            this.commandManage = commandManage;
        }

        @Override
        public boolean onCommand(CommandSender commandSender,Command command,String s,String[] strings) {
            CommandInterface ci = commandManage.commandMap.get(command.getName());
            if (ci == null) return false;
            String[] args = new String[strings.length + 1];
            args[0] = command.getName();
            System.arraycopy(strings,0,args,1,strings.length);
            ci.onCommand(commandSender,command,s,args);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender commandSender,Command command,String s,String[] strings) {
            CommandInterface ci = commandManage.commandMap.get(command.getName());
            if (ci == null) return null;
            String[] args = new String[strings.length + 1];
            args[0] = command.getName();
            System.arraycopy(strings,0,args,1,strings.length);
            return ci.onTabComplete(commandSender,command,s,args);
        }
    }
}
