package cn.whiteg.memfree;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class HasCommandInterface extends CommandInterface {
    public abstract boolean executor(CommandSender sender,Command cmd,String str,String[] args);

    public List<String> completer( CommandSender sender, Command cmd, String str, String[] args) {
        return PlayersList(args);
    }

    @Override
    public boolean onCommand( CommandSender sender, Command cmd, String str, String[] args) {
        if (canUseCommand(sender)){
            return executor(sender,cmd,str,args);
        } else {
            sender.sendMessage("§b阁下当前不能使用这条指令");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        if (canUseCommand(sender)){
            return completer(sender,cmd,label,args);
        } else return null;
    }
}
