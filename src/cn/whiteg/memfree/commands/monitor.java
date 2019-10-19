package cn.whiteg.memfree.commands;

import cn.whiteg.memfree.MemFree;
import cn.whiteg.mmocore.CommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.List;

public class monitor extends CommandInterface implements Listener {
    private CommandSender mder;
    private MemFree plugin;

    public monitor() {
        plugin = MemFree.plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        mder.sendMessage(event.isNewChunk() ? "生成区块" : "加载区块" + event.getChunk().toString());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        mder.sendMessage("卸载区块" + event.getChunk().toString());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (mder == null) return;
        if (mder instanceof Player){
            if (event.getPlayer().getName().equals(mder.getName())){
                unreg();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        if (!sender.hasPermission("memfree.gc")){
            sender.sendMessage("没有权限");
            return true;
        }
        reg(sender);
        return true;
    }

    public void unreg() {
        ChunkLoadEvent.getHandlerList().unregister(this);
        ChunkUnloadEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        mder.sendMessage("停止监听");
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
        sender.sendMessage("开始监听事件");
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
