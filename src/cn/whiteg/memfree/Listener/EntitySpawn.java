package cn.whiteg.memfree.Listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.lang.ref.WeakReference;
import java.util.Collection;

import static cn.whiteg.memfree.MemFree.logger;
import static cn.whiteg.memfree.Setting.*;

public class EntitySpawn implements Listener {
    //同步执行器
//    Executor syncExecutor = new Executor() {
//        @Override
//        public void execute(Runnable runnable) {
//            Bukkit.getScheduler().runTask(MemFree.plugin,() -> {
//                runnable.run();
//                synchronized (runnable) {
//                    runnable.notify();
//                }
//            });
//            synchronized (runnable) {
//                try{
//                    runnable.wait();
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    };
    private WeakReference<Chunk> cacheChunk = new WeakReference<>(null);
    private int number = 0;
    // private Map<EntityType, Integer> ets = new EnumMap<EntityType, Integer>(EntityType.class);
    private int entitynum = 0;
    private EntityType cacheEntityType;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER) return;
        Chunk chunk = entity.getLocation().getChunk();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        if (chunk != cacheChunk.get() && entity.getType() != cacheEntityType){
            if (DEBUG){
                logger.info("搜索");
            }
            number = 0;
            entitynum = 0;
            Collection<Entity> els = entity.getNearbyEntities(MaxEntity_Range,128,MaxEntity_Range);
            for (Entity e : els) {
                if (e.getType() == entity.getType()) entitynum++;
                number++;
            }
            cacheChunk = new WeakReference<>(chunk);
            cacheEntityType = entity.getType();
        } else if (DEBUG) logger.info("跳过 ： 区块相同");
        EntityType et = entity.getType();
        //   if (number > (MemFree.MaxEntity.containsKey(et) ? (ets.containsKey(et) ? ets.get(et) :  MemFree.MaxEntity.get(et)) : MemFree.AllEntity)){
        if (number > AllEntity || entitynum > MaxEntity.getOrDefault(et,DefMaxEntity)){
            event.setCancelled(true);
            //entity.remove();
              /*  if (et == EntityType.DROPPED_ITEM)
                    logger.info("以阻止 " + chunk.getWorld() + " " + chunkX + " " + chunkZ + " 的掉落物" + ((Item) event.getEntity()).getItemStack().getType() + "生成 , 该区块实体数量达到" + number + " 同类型数量" + ets.getOrDefault(et,1));
                else
               */
            if (DEBUG){
                logger.info("以阻止 " + chunk.getWorld() + " " + chunkX + " " + chunkZ + " 的实体" + entity.getType().toString() + "生成 , 该区块实体数量达到" + number + " 同类型数量" + entitynum);
            }
        } else if (DEBUG){
            logger.info("区块 " + chunk.getWorld() + " " + chunkX + " " + chunkZ + " 实体" + entity.getType().toString() + "生成 , 区块实体数量" + number + " 同类型数量" + entitynum);
        }
    }
}
