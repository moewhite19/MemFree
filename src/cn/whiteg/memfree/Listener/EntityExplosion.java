package cn.whiteg.memfree.Listener;

import cn.whiteg.chanlang.LangUtils;
import cn.whiteg.memfree.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.EnumMap;
import java.util.Map;

public class EntityExplosion implements Listener {
    @EventHandler
    public void chunkLoad(ChunkLoadEvent event) {
        if (Setting.entityExplosion <= 0) return;
        final Chunk chunk = event.getChunk();
        var entitys = chunk.getEntities();
        if (entitys.length > Setting.entityExplosion){
            var map = new EnumMap<EntityType, Integer>(EntityType.class);
            broadcast("§b区块§f" + chunk.getWorld().getName() + chunk.getX() + ", " + chunk.getZ() + "§b 的实体数量超出上限,达到§f" + entitys.length + "§b，开始执行清理程序");
            //第一次循环找出最多的实体类型
            for (Entity entity : entitys) {
                var t = entity.getType();
                map.put(t,map.getOrDefault(t,0) + 1);
            }

            int num = 0;
            EntityType type = null;
            for (Map.Entry<EntityType, Integer> entry : map.entrySet()) {
                if (entry.getValue() > num){
                    num = entry.getValue();
                    type = entry.getKey();
                }
            }

            if (type == null){
                broadcast("出现内部错误Null");
            } else {
                broadcast("清理" + LangUtils.getEntityTypeName(type) + "x" + num);
                //第三次循环清理掉记录的最多的实体
                for (Entity entity : entitys) {
                    if (entity.getType().equals(type)){
                        entity.remove();
                    }
                }
            }

        }
    }

    public void broadcast(String msg) {
        Bukkit.broadcastMessage(msg);
    }
}
