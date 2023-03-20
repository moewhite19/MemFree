package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class test extends HasCommandInterface {
    final String p = "[System.gc()]";

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {

        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return false;
        }
        if (args.length == 1){
            try{
                int inx = Integer.parseInt(args[0]);
                createThear(sender,inx,5);
            }catch (NumberFormatException e){
                sender.sendMessage("参数有误");
            }
        } else if (args.length == 2){
            try{
                int inx = Integer.parseInt(args[0]);
                int time = Integer.parseInt(args[1]);
                createThear(sender,inx,time);
            }catch (NumberFormatException e){
                sender.sendMessage("参数有误");
            }
        } else {
            createThear(sender,1,5);
        }
        return true;
    }

    public void createThear(final CommandSender sender,final int count,final int time) {
        final int finalTime = time * 1000;
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + finalTime;
                long index = 0;
                while (true) {
                    final long now = System.currentTimeMillis();
                    final double d = Math.sqrt(index);
                    if (now > endTime) break;
                    index++;
                }
                final String str = String.valueOf(index);
                sender.sendMessage(time + "秒运算了 " + str + " 次 " + str.length() + " 位数");
            }).start();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "测试服务器性能";
    }
}
