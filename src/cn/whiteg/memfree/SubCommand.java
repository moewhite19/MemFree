package cn.whiteg.memfree;

import cn.whiteg.mmocore.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommand extends CommandInterface {
    public String[] subCmds = new String[]{"gc"};

    @Override
    public boolean onCommand(CommandSender commandSender,Command command,String s,String[] strings) {
        CommandInterface ci = MemFree.plugin.mainCmd.CommandMap.get(command.getName());
        if (ci == null) return false;
        String[] args = new String[strings.length + 1];
        args[0] = command.getName();
/*        for(int i = 0 ; i < args.length ; i++){
            args[i + 1] = strings[i] ;
        }*/
        System.arraycopy(strings,0,args,1,strings.length);
        ci.onCommand(commandSender,command,s,args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender,Command command,String s,String[] strings) {
        CommandInterface ci = MemFree.plugin.mainCmd.CommandMap.get(command.getName());
        if (ci == null) return null;
        String[] args = new String[strings.length + 1];
        args[0] = command.getName();
/*        for(int i = 0 ; i < args.length ; i++){
            args[i + 1] = strings[i] ;
        }*/

        System.arraycopy(strings,0,args,1,strings.length);
        return ci.onTabComplete(commandSender,command,s,args);
    }
}
