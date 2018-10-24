package cn.whiteg.memfree;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;


public class playerexit
        implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void playerquit(PlayerQuitEvent paramInventoryClickEvent) {
        //Player who = paramInventoryClickEvent.getPlayer();
        //
    }
}

