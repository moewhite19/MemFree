/*    */
package cn.whiteg.memfree;
/*    */
/*    */

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

public class CommandManager implements CommandExecutor, TabCompleter {

    public boolean onCommand(CommandSender sender,Command Command,String paramString,String[] paramArrayOfString) {

        if (!Command.getName().equalsIgnoreCase("MemFree")) {
            return false;
        }
        boolean is = MemFree.in().timer.getTimer();
        //Player player = Bukkit.getPlayerExact(paramCommandSender.getName());
        if (paramArrayOfString.length != 1) {
            sender.sendMessage("§bMemFree §2by §f某白");
            if (is){
                sender.sendMessage("§b当前计时器§a已启用");
            }else {
                sender.sendMessage("§b当前计时器§7未启用");
            }
            long max = Runtime.getRuntime().maxMemory();
            long use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long free = max - use;
            long minfree = MemFree.in().minfree;
            sender.sendMessage("§b最小内存§3:§r " + minfree / 1024 / 1024 + "MB");
            sender.sendMessage("§b剩余内存§3:§r " + free / 1024 / 1024 + "MB");

            return true;
        }
        if (paramArrayOfString[0].equalsIgnoreCase("reload")) {
            if ((!sender.hasPermission("whiteg.reload")) || (!sender.isOp())) {
                sender.sendMessage("§b阁下没有权限使用这个指令");
                return true;
            }
            MemFree.in().reloadConfig();
            MemFree.in().minfree = MemFree.in().getConfig().getLong("minfree") * 1024 * 1024;
            sender.sendMessage("§b重载");
            MemFree.in().timer.stopTimer();
            MemFree.in().timer.setTimer();
            return true;
        }
        if (paramArrayOfString[0].equalsIgnoreCase("toggle")) {
            if (sender.hasPermission("whiteg.set")) {
                if (is){
                    MemFree.in().timer.stopTimer();
                }else{
                    MemFree.in().timer.setTimer();
                }
                return true;
            } else {
                sender.sendMessage("§b阁下没有权限使用这个指令");
                return true;
            }
        }  else {

        }
        return true;
    }
    public List<String> onTabComplete(CommandSender paramCommandSender,Command paramCommand,String
            paramString,String[] paramArrayOfString)
   {
        for (int i = 0; i < paramArrayOfString.length; i++) {
            paramArrayOfString[i] = paramArrayOfString[i].toLowerCase();
        }
        if (paramArrayOfString.length == 1) {
            ArrayList arr = new ArrayList();
            arr.add("reload");
            arr.add("toggle");
            return arr;
        }
        return null;
    }
}