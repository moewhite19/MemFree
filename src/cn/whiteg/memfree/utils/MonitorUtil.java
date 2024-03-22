package cn.whiteg.memfree.utils;

import cn.whiteg.memfree.reflection.FieldAccessor;
import com.destroystokyo.paper.util.misc.PlayerAreaMap;
import io.papermc.paper.chunk.system.RegionizedPlayerChunkLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.function.Supplier;


public class MonitorUtil {
    private static FieldAccessor<double[]> recentTps;
    private static DedicatedServer con;
    private static FieldAccessor<ChunkProviderServer> chunkProvider;
    private static FieldAccessor<RegionizedPlayerChunkLoader> regionChunkLoader;
    private static FieldAccessor<PlayerChunkMap> playerChunkMap;
    private static FieldAccessor<Integer> viewDistance;
    private static FieldAccessor<PathfinderGoalSelector>[] pathfinderGoalSelectors;
    private static FieldAccessor<World> entityWorld;
    private static Method getGameProfilerFiller;

    private static FieldAccessor<WorldServer> nmsWorldField;
    private static FieldAccessor<Entity> nmsEntityField;
    private static Map<World, PathfinderGoalSelector> pathCacheMap = new WeakHashMap<>();

    public static String SERVER_VER;

    static {
        final Server ser = Bukkit.getServer();
        final String packageName = ser.getClass().getName();
        final String CRAFT_ROOT = "org.bukkit.craftbukkit." + packageName.split("\\.")[3]; //服务端版本号
        try{
            final Field console_f = MonitorUtil.class.getClassLoader().loadClass(CRAFT_ROOT + ".CraftServer").getDeclaredField("console");
            console_f.setAccessible(true);
            con = (DedicatedServer) console_f.get(ser);
            recentTps = new FieldAccessor<>(MinecraftServer.class.getDeclaredField("recentTps"));
            chunkProvider = new FieldAccessor<>(NMSUtils.getFieldFormType(WorldServer.class,ChunkProviderServer.class));
            playerChunkMap = new FieldAccessor<>(NMSUtils.getFieldFormType(ChunkProviderServer.class,PlayerChunkMap.class));
            viewDistance = new FieldAccessor<>(NMSUtils.getFieldFormStructure(PlayerChunkMap.class,Queue.class,int.class)[1]);
            regionChunkLoader = new FieldAccessor<>(NMSUtils.getFieldFormType(WorldServer.class,RegionizedPlayerChunkLoader.class));

            ArrayList<FieldAccessor<PathfinderGoalSelector>> list = new ArrayList<>(3);
            for (Field field : EntityInsentient.class.getFields()) {
                if (field.getType().equals(PathfinderGoalSelector.class)){
                    list.add(new FieldAccessor<>(field));
                }
            }
            //ToArrayCallWithZeroLengthArrayArgument
            pathfinderGoalSelectors = list.toArray(new FieldAccessor[list.size()]);
            nmsWorldField = new FieldAccessor<>(NMSUtils.getFieldFormType(MonitorUtil.class.getClassLoader().loadClass(CRAFT_ROOT + ".CraftWorld"),WorldServer.class));
            nmsEntityField = new FieldAccessor<>(NMSUtils.getFieldFormType(MonitorUtil.class.getClassLoader().loadClass(CRAFT_ROOT + ".entity.CraftEntity"),Entity.class));
            entityWorld = new FieldAccessor<>(NMSUtils.getFieldFormType(Entity.class,World.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Field field = NMSUtils.getFieldFormType(SharedConstants.class,WorldVersion.class);
            field.setAccessible(true);
            MinecraftVersion ver = (MinecraftVersion) field.get(null);
            final Field getNameField = NMSUtils.getFieldFormType(MinecraftVersion.class,String.class);
            getNameField.setAccessible(true);
            SERVER_VER = (String) getNameField.get(ver);
        }catch (IllegalAccessException | NoSuchFieldException e){
            e.printStackTrace();
            SERVER_VER = e.getMessage();
        }

        for (Method method : World.class.getMethods()) {
            if (method.getReturnType().equals(GameProfilerFiller.class)){
                getGameProfilerFiller = method;
                break;
            }
        }
    }

    public static double[] getTps() {
        try{
            //con.setMotd(name);
            return recentTps.get(con);
        }catch (Exception e){
            return null;
        }
    }

    public static int getDistance(final Player player) {
        return player.getViewDistance();
    }

    public static void setDistance(final Player player,final int d) {
        player.setViewDistance(d);
        player.setSendViewDistance(d);

//        cp.getClientViewDistance();
//        EntityPlayer nmsplayer = cp.getHandle();
//        nmsplayer.clientViewDistance = d;
    }

    public static int getDistance(final org.bukkit.World world) {
        if (world == null) return 0;
        try{
            WorldServer ws = nmsWorldField.get(world);
            ChunkProviderServer cps = chunkProvider.get(ws);
            PlayerChunkMap pcm = playerChunkMap.get(cps);
            //m = pcm.getClass().getMethod("")
            return viewDistance.get(pcm);
        }catch (Exception e){
            return 0;
        }
    }

    public static void setDistance(final org.bukkit.World world,int viewDistance) {
        if (world == null) return;
        try{
            viewDistance = Math.max(2,Math.min(48,viewDistance)); //限制范围2 - 48
            var ws = nmsWorldField.get(world);
            ChunkProviderServer cps = chunkProvider.get(ws);
            PlayerChunkMap pcm = playerChunkMap.get(cps);
            MonitorUtil.viewDistance.set(pcm,viewDistance);
            final RegionizedPlayerChunkLoader chunkLoader = regionChunkLoader.get(ws);
            chunkLoader.setSendDistance(viewDistance);
            chunkLoader.setLoadDistance(viewDistance + 1);
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
        if (isNoAI(entity)) return;
        EntityInsentient ne = (EntityInsentient) getNmsEntity(entity);
        if (ne == null) return;
//        PathfinderGoalSelector p = new PathfinderGoalSelector(null);
        final PathfinderGoalSelector goalSelector = createPathfinderGoalSelector(ne);
        if (goalSelector == null) entity.remove();
        for (FieldAccessor<PathfinderGoalSelector> pathfinderGoalSelector : pathfinderGoalSelectors) {
            pathfinderGoalSelector.set(ne,goalSelector);
        }
    }

    public static boolean isNoAI(Mob entity) {
        EntityInsentient ne = (EntityInsentient) getNmsEntity(entity);
        if (ne != null) for (FieldAccessor<PathfinderGoalSelector> pathfinderGoalSelector : pathfinderGoalSelectors) {
            final PathfinderGoalSelector goalSelector = pathfinderGoalSelector.get(ne);
            return goalSelector instanceof NoTickPathfinder;
        }
        return false;
    }

    public static PathfinderGoalSelector createPathfinderGoalSelector(EntityInsentient ne) {
        var world = entityWorld.get(ne);
        return pathCacheMap.computeIfAbsent(world,world1 -> {
            GameProfilerFiller mp;
            try{
                mp = (GameProfilerFiller) getGameProfilerFiller.invoke(world);
            }catch (IllegalAccessException | InvocationTargetException e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Supplier<GameProfilerFiller> supplier = () -> mp;
            if (mp == null){
                return null;
            }
            return new NoTickPathfinder(supplier);
        });
    }


    public static void killMe() {
        Runtime.getRuntime().halt(9);
    }

    public static Entity getNmsEntity(org.bukkit.entity.Entity entity) {
        return nmsEntityField.get(entity);
    }

    public static class NoTickPathfinder extends PathfinderGoalSelector {
        public NoTickPathfinder(Supplier<GameProfilerFiller> supplier) {
            super(supplier);
        }

        @Override
        // public void doTick()
        public void a() {
        }
    }
}
