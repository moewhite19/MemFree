package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.MonitorUtil;
import cn.whiteg.mmocore.CommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

public class CommandManage extends CommandInterface {
    public Map<String, CommandInterface> CommandMap = new HashMap();
    public List<String> AllCmd;

    public CommandManage() {
        AllCmd = Arrays.asList("reload","clear","toggle","gc","onrestart","shell","monitor","distance","pdistance","test");
        for (int i = 0; i < AllCmd.size(); i++) {
            try{
                Class c = Class.forName("cn.whiteg.memfree.commands." + AllCmd.get(i));
                regCommand(AllCmd.get(i),(CommandInterface) c.newInstance());
            }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
                MemFree.logger.warning("没有注册指令" + AllCmd.get(i) + ": " + e.getMessage());
            }
        }
    }

    public static List<String> getMatches(String[] args,List<String> list) {
        return getMatches(args[args.length - 1],list);
    }

    public static List<String> getMatches(List<String> list,String[] args) {
        return getMatches(args[args.length - 1],list);
    }

    public static List<String> getMatches(String value,List<String> list) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i).intern().toLowerCase();
            if (str.startsWith(value.toLowerCase())){
                result.add(list.get(i));
            }
        }
        return result;
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
        CommandInterface c = CommandMap.get(args[0]);
        if (c != null){
            return c.onCommand(sender,cmd,label,args);
        } else {
            sender.sendMessage("无效指令");
        }
        return true;
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
        sender.sendMessage("§b服务器已运行§3:§r " + mintoh(Math.round((double) (System.currentTimeMillis() - MemFree.runtime) / 1000 / 60)) + "  §3线程数:§f" + threadcount);
        sender.sendMessage("§b内存使用§3:§f " + max / 1024 / 1024 + "MB§7-§f" + use / 1024 / 1024 + "MB §7= §f" + MemFree.plugin.timer.Mem / 1024 / 1024 + "§7MB " + "  §b已启用: §f" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "§7MB");
        final long dt = ser.getWorldContainer().getTotalSpace();
        final long du = ser.getWorldContainer().getUsableSpace();
        final long duse = dt - du;
        sender.sendMessage("§b磁盘空间§3:§f " + dfg(dt) + "-" + dfg(duse) + "=" + dfg(du));
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

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length > 1){
            List ls = null;
            if (CommandMap.containsKey(args[0])) ls = CommandMap.get(args[0]).onTabComplete(sender,cmd,label,args);
            if (ls != null){
                return getMatches(args[args.length - 1],ls);
            }
        }
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
        if (args.length == 1){
            return getMatches(args[0],AllCmd);
        }
        return null;
    }

    public void regCommand(String var1,CommandInterface cmd) {
        CommandMap.put(var1,cmd);
    }

    public String mintoh(long s) {
        final StringBuilder sb = new StringBuilder();
        if (s < 60){
            return sb.append(s).append("分钟").toString();
        }
        int m = 0;
        while (s >= 60) {
            s -= 60;
            m++;
        }
        sb.append(m).append("小时");
        if (s > 0) sb.append(s).append("分钟");
        return sb.toString();
    }

    String dfg(long l) {
        if (l <= 0) return "NaN";
        final double k = 1024D;
        final DecimalFormat df = new DecimalFormat("#.00");
        if (l <= k){
            return df.format(l) + "B";
        }
        final double m = k * k;
        if (l <= m){
            return df.format(l / k) + "KB";
        }
        final double g = m * k;
        if (l <= g){
            return df.format(l / m) + "MB";
        }
        return df.format(l / g) + "GB";
    }
}
