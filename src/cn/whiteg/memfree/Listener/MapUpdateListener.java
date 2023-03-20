package cn.whiteg.memfree.Listener;

import cn.whiteg.memfree.Setting;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.io.File;

public class MapUpdateListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onLoadChunk(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof ItemFrame itemFrame){
                update(itemFrame.getItem());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().forEach((this::update));
    }

    public void update(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta instanceof MapMeta){
            MapMeta mapMeta = (MapMeta) meta;
            MapView mapView = mapMeta.getMapView();
            if (mapView != null){
                int id = mapView.getId();
                File file = new File(Setting.WORLD_DATA_DIR,"map_" + id + ".dat");
                if (file.exists()){
                    file.setLastModified(System.currentTimeMillis());
                }
            }
        }
    }
}
