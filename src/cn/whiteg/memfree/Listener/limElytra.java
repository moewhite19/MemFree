package cn.whiteg.memfree.Listener;

import cn.whiteg.memfree.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;

public class limElytra implements Listener {
    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (olinePlayer() < Setting.limElytra) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final ItemStack item = event.getItem();
        if (item == null) return;
        final Player player = event.getPlayer();
        if (item.getType() == Material.FIREWORK_ROCKET){
            final ItemStack h = player.getInventory().getChestplate();
            if (h != null && h.getType() == Material.ELYTRA){
                event.setCancelled(true);
                player.sendMessage("当前服务器人数大于" + Setting.limElytra + " 的时候阁下不能使用这个物品加速鞘翅");
                final Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(),EntityType.FIREWORK);
                if (item.hasItemMeta()){
                    FireworkMeta fm = (FireworkMeta) item.getItemMeta();
                    fw.setFireworkMeta(fm);
                }
                if (item.getAmount() > 1){
                    item.setAmount(item.getAmount() - 1);
                } else {
                    if (event.getHand() == EquipmentSlot.HAND){
                        player.getInventory().setItemInMainHand(null);
                    } else if (event.getHand() == EquipmentSlot.OFF_HAND){
                        player.getInventory().setItemInOffHand(null);
                    }
                }

            }
        }
    }
    @EventHandler
    public void onRiptid(PlayerRiptideEvent event) {
        if (olinePlayer() < Setting.limElytra) return;
        final PlayerInventory pi = event.getPlayer().getInventory();
        final ItemStack h = pi.getChestplate();
        if (h != null && h.getType() == Material.ELYTRA){
            pi.setChestplate(null);
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),h);
            event.getPlayer().sendMessage("当前服务器人数大于" + Setting.limElytra + " 的时候阁下不能使用这个物品");
        }
    }

    private int olinePlayer() {
        return Bukkit.getOnlinePlayers().size();
    }
//    @EventHandler
//    public void onClickInv(InventoryClickEvent event){
//        if(olinePlayer()<=maxPlayer)return;
//        PlayerInventory inv = event.getWhoClicked().getInventory();
//        ItemStack item = inv.getChestplate();
//        if(item!=null && item.getType()==Material.ELYTRA){
//            HumanEntity human = event.getWhoClicked();
//            Location loc = event.getWhoClicked().getLocation();
//            inv.setChestplate(null);
//            loc.getWorld().dropItem(loc , item);
//            human.sendMessage("当前服务器人数大于" + maxPlayer + " 的时候阁下不能使用鞘翅");
//        }
//    }

//    @EventHandler
//    public void on(ArmourChangeEvent event){
//        if(olinePlayer()<=maxPlayer)return;
//        ItemStack item = event.getItem();
//        if(item == null)return;
//        if(item.getType()==Material.ELYTRA){
//            event.setCancelled(true);
//        }
//    }
}
