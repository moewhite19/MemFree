package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.HasCommandInterface;
import cn.whiteg.memfree.MemFree;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class monitor extends HasCommandInterface implements Listener {
    private CommandSender mder;
    private MemFree plugin;
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
            unreg();
            sender.sendMessage("取消监听");
            return true;
        }
        try{
            if (args.length == 3){
                x = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
            } else if (args.length == 2){
                x = Integer.MAX_VALUE;
            } else if (sender instanceof Player){
                Chunk c = ((Player) sender).getLocation().getChunk();
                x = c.getX();
                z = c.getZ();
            }
        }catch (NumberFormatException e){
            sender.sendMessage("参数有误");
        }
        sender.sendMessage("开始监听区块");
        reg(sender);
        return true;
    }

    public void unreg() {
        ChunkLoadEvent.getHandlerList().unregister(this);
        ChunkUnloadEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
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
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("whiteg.test");
    }
}
