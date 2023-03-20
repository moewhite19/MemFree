package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;
import java.util.List;

public class monitor extends HasCommandInterface implements Listener {
    private CommandSender mder;
    private MemFree plugin;
    private World world;
    private int x = 0;
    private int z = 0;

    public monitor(MemFree pl) {
        plugin = pl;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk c = event.getChunk();
        if (x != Integer.MAX_VALUE && (x != c.getX() || z != c.getZ())){
            return;
        }
        mder.sendMessage(event.isNewChunk() ? "生成区块" : "加载区块" + c.toString());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk c = event.getChunk();
        if (x != Integer.MAX_VALUE && (x != c.getX() || z != c.getZ())){
            return;
        }
        mder.sendMessage("卸载区块" + c.toString());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (mder instanceof Player){
            if (mder == event.getPlayer()){
                unreg();
            }
        }
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String label,String[] args) {
        if (mder != null){
            if (args.length == 1){
                var arg = args[0];
                switch (arg) {
                    case "load" -> {
                        var chunk = world.getChunkAt(x,z);
                        chunk.load(true);
                        sender.sendMessage("尝试加载区块");
                    }
                    case "unload" -> {
                        var chunk = world.getChunkAt(x,z);
                        chunk.unload(true);
                        sender.sendMessage("尝试卸载区块");
                    }
                    default -> {
                        sender.sendMessage("无效参数" + arg);
                    }
                }
                return true;
            }

            unreg();
            sender.sendMessage("取消监听");
            return true;
        }


        try{
            if (args.length == 3){
                world = Bukkit.getWorld(args[0]);
                if (world == null){
                    sender.sendMessage("找不到世界" + args[0]);
                    return false;
                }
                x = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
            } else if (sender instanceof Player player){
                Chunk c = player.getChunk();
                x = c.getX();
                z = c.getZ();
                world = c.getWorld();
            }
        }catch (NumberFormatException e){
            sender.sendMessage("参数有误");
        }
        sender.sendMessage("开始监听区块" + x + "," + z);
        reg(sender);
        return true;
    }

    public void unreg() {
        HandlerList.unregisterAll(this);
        mder = null;
    }


    public void reg(CommandSender sender) {
        if (mder != null){
            if (sender.getName().equals(mder.getName())){
                unreg();
                return;
            }
            unreg();
        }
        mder = sender;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length == 1) return Arrays.asList("load","unload");
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }

    @Override
    public String getDescription() {
        return "监听区块加载(调试用)";
    }
}
