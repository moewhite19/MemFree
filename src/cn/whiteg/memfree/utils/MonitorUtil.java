package cn.whiteg.memfree.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.libs.jline.internal.ShutdownHooks;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMob;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;


public class MonitorUtil {
    private static Field tpsf;
    private static DedicatedServer con;
    private static Field chunkProvider;
    private static Field playerChunkMap;
    private static Field viewDistance;

    static {
        final Server ser = Bukkit.getServer();
        Class<CraftServer> craftServer = CraftServer.class;
        try{
            final Field console_f = craftServer.getDeclaredField("console");
            console_f.setAccessible(true);
            con = (DedicatedServer) console_f.get(ser);
            tpsf = MinecraftServer.class.getDeclaredField("recentTps");
            tpsf.setAccessible(true);
            chunkProvider = WorldServer.class.getDeclaredField("C");
            chunkProvider.setAccessible(true);
            playerChunkMap = ChunkProviderServer.class.getDeclaredField("a");
            playerChunkMap.setAccessible(true);
            viewDistance = PlayerChunkMap.class.getDeclaredField("J");
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
            WorldServer ws = ((CraftWorld) world).getHandle();
            ChunkProviderServer cps = (ChunkProviderServer) chunkProvider.get(ws);
            PlayerChunkMap pcm = (PlayerChunkMap) playerChunkMap.get(cps);
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
        var world = ne.t;
        if (world == null){
            entity.remove();
            return;
        }
        GameProfilerFiller mp = world.getMethodProfiler();
        Supplier<GameProfilerFiller> supplier = new Supplier<>() {
            @Override
            public GameProfilerFiller get() {
                return mp;
            }
        };
        if (mp == null){
            entity.remove();
        }
        PathfinderGoalSelector p = new PathfinderGoalSelector(supplier);
        ne.bO = p;
        ne.bP = p;
    }


    public static void clearShutdownHooks() {
        try{
            var f = ShutdownHooks.class.getDeclaredField("tasks");
            f.setAccessible(true);
            List<ShutdownHooks.Task> list = (List<ShutdownHooks.Task>) f.get(false);
            list.clear();
        }catch (NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    public static void forgeStop() {
        clearShutdownHooks();
        var tt = Thread.currentThread();
        Thread.getAllStackTraces().forEach((thread,stackTraceElements) -> {
            if (thread == tt) return;
            try{
                thread.stop();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public static void killMe() {
        File file = new File("kill.sh");
        String path;
        try{
            path = file.getCanonicalPath();
        }catch (IOException e){
            path = file.getPath();
        }
        if (file.exists()){
            System.out.println("run " + path);
            try{
                Runtime.getRuntime().exec(path);
            }catch (IOException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("no fire: " + path);
            forgeStop();
        }
    }
}
