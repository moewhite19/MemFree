package cn.whiteg.memfree.utils;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftMob;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MonitorUtil {
    private static Field tpsf;
    private static DedicatedServer con;
    private static Field chunkProvider;
    private static Field playerChunkMap;
    private static Field viewDistance;

    static {
        final Server ser = Bukkit.getServer();
        try{
            final Field console_f = ser.getClass().getDeclaredField("console");
            console_f.setAccessible(true);
            con = (DedicatedServer) console_f.get(ser);
            tpsf = MinecraftServer.class.getDeclaredField("recentTps");
            tpsf.setAccessible(true);
            chunkProvider = World.class.getDeclaredField("chunkProvider");
            chunkProvider.setAccessible(true);
            playerChunkMap = ChunkProviderServer.class.getDeclaredField("playerChunkMap");
            playerChunkMap.setAccessible(true);
            viewDistance = PlayerChunkMap.class.getDeclaredField("viewDistance");
            viewDistance.setAccessible(true);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static double[] getTps() {
        try{
            //con.setMotd(name);
            return (double[]) tpsf.get(con);
        }catch (Exception e){
            return null;
        }
    }

    public static int getDistance(final Player player) {
        return player.getClientViewDistance();
    }

    public static void setDistance(final Player player,final int d) {
        final CraftPlayer cp = (CraftPlayer) player;
        cp.getClientViewDistance();
        EntityPlayer nmsplayer = cp.getHandle();
        nmsplayer.clientViewDistance = d;

    }

    public static int getDistance(final org.bukkit.World world) {
        if (world == null) return 0;
        try{
            WorldServer ws = ((CraftWorld) world).getHandle();
            ChunkProviderServer cps = (ChunkProviderServer) chunkProvider.get(ws);
            PlayerChunkMap pcm = (PlayerChunkMap) playerChunkMap.get(cps);
            //m = pcm.getClass().getMethod("")
            return (int) viewDistance.get(pcm);
        }catch (Exception e){
            return 0;
        }
    }

    public static void setDistance(final org.bukkit.World world,final int cd) {
        if (world == null) return;
        try{
            final WorldServer ws = ((CraftWorld) world).getHandle();
            Field f = net.minecraft.server.v1_15_R1.World.class.getDeclaredField("chunkProvider");
            f.setAccessible(true);
            final ChunkProviderServer cps = (ChunkProviderServer) f.get(ws);
            f = cps.getClass().getDeclaredField("playerChunkMap");
            f.setAccessible(true);
            PlayerChunkMap pcm = (PlayerChunkMap) f.get(cps);
            f = pcm.getClass().getDeclaredField("viewDistance");
            f.setAccessible(true);
            final Method m = pcm.getClass().getDeclaredMethod("setViewDistance",int.class);
            m.setAccessible(true);
            m.invoke(pcm,cd);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setDistance(final int cd) {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            setDistance(world,cd);
        }
    }

    public static void clearEntityAI(Mob entity) {
        EntityInsentient ne = (((CraftMob) entity).getHandle());
//        PathfinderGoalSelector p = new PathfinderGoalSelector(null);
        if (ne.world == null){
            entity.remove();
        }
        GameProfilerFiller mp = ne.world.getMethodProfiler();
        if (mp == null){
            entity.remove();
        }
        PathfinderGoalSelector p = new PathfinderGoalSelector(mp);
        ne.goalSelector = p;
        ne.targetSelector = p;
    }

}
