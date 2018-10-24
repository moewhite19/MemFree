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

    public boolean onCommand(CommandSender paramCommandSender,Command paramCommand,String paramString,String[] paramArrayOfString) {

        if (!paramCommand.getName().equalsIgnoreCase("MemFree")) {
            return false;
        }
        boolean is = MemFree.in().timer.getTimer();
        if (paramArrayOfString.length != 1) {
            paramCommandSender.sendMessage("MemFree by 某白");
            if (is){
                paramCommandSender.sendMessage("当前计时器§a已启用");
            }else {
                paramCommandSender.sendMessage("当前计时器§7未启用");
            }
            return true;
        }
        Player localPlayer = Bukkit.getPlayerExact(paramCommandSender.getName());
        if (paramArrayOfString[0].equalsIgnoreCase("reload")) {
            if ((!paramCommandSender.hasPermission("whiteg.reload")) || (!paramCommandSender.isOp())) {
                paramCommandSender.sendMessage("阁下没有权限使用这个指令");
                return true;
            }
            MemFree.in().reloadConfig();
            MemFree.in().minfree = MemFree.in().getConfig().getLong("minfree") * 1024 * 1024;
            paramCommandSender.sendMessage("重载");
            MemFree.in().timer.stopTimer();
            MemFree.in().timer.setTimer();
            return true;
        }
        Object localObject1;
        Object localObject2;
        if (paramArrayOfString[0].equalsIgnoreCase("toggle")) {
            if (paramCommandSender.hasPermission("whiteg.set")) {
                if (is){
                    MemFree.in().timer.stopTimer();
                }else{
                    MemFree.in().timer.setTimer();
                }
                return true;
            } else {
                paramCommandSender.sendMessage("阁下没有权限");
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