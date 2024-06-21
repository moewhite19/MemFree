package cn.whiteg.memfree.utils;

import ca.spottedleaf.moonrise.patches.chunk_system.player.RegionizedPlayerChunkLoader;
import cn.whiteg.memfree.reflection.FieldAccessor;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;


public class MonitorUtil {
    private static FieldAccessor<double[]> recentTps;
    private static DedicatedServer con;
    private static FieldAccessor<ServerChunkCache> chunkProvider;
    private static FieldAccessor<ChunkMap> playerChunkMap;
    private static FieldAccessor<Integer> viewDistance;
    private static FieldAccessor<GoalSelector>[] pathfinderGoalSelectors;
    private static FieldAccessor<ServerLevel> entityWorld;
    private static FieldAccessor<RegionizedPlayerChunkLoader> regionChunkLoader;
    private static Method getProfilerFiller;

    private static FieldAccessor<ServerLevel> nmsWorldField;
    private static FieldAccessor<Entity> nmsEntityField;
    private static Map<ServerLevel, GoalSelector> pathCacheMap = new WeakHashMap<>();

    public static String SERVER_VER;


    static {
        final Server ser = Bukkit.getServer();
//        final String packageName = ser.getClass().getName();
//        final String CRAFT_ROOT = "org.bukkit.craftbukkit." + packageName.split("\\.")[3]; //服务端版本号(spigot)
        try{
            final Field console_f = CraftServer.class.getDeclaredField("console");
            console_f.setAccessible(true);
            con = (DedicatedServer) console_f.get(ser);
            recentTps = new FieldAccessor<>(MinecraftServer.class.getDeclaredField("recentTps"));
            chunkProvider = new FieldAccessor<>(NMSUtils.getFieldFormType(ServerLevel.class,ServerChunkCache.class));
            playerChunkMap = new FieldAccessor<>(NMSUtils.getFieldFormType(ServerChunkCache.class,ChunkMap.class));
            viewDistance = new FieldAccessor<>(ChunkMap.class.getDeclaredField("serverViewDistance"));
            regionChunkLoader = new FieldAccessor<>(NMSUtils.getFieldFormType(ServerLevel.class,RegionizedPlayerChunkLoader.class));

            ArrayList<FieldAccessor<GoalSelector>> list = new ArrayList<>(3);
            for (Field field : net.minecraft.world.entity.Mob.class.getFields()) {
                if (field.getType().equals(GoalSelector.class)){
                    list.add(new FieldAccessor<>(field));
                }
            }
            //ToArrayCallWithZeroLengthArrayArgument
            pathfinderGoalSelectors = list.toArray(new FieldAccessor[list.size()]);
            nmsWorldField = new FieldAccessor<>(NMSUtils.getFieldFormType(CraftWorld.class,ServerLevel.class));
            nmsEntityField = new FieldAccessor<>(NMSUtils.getFieldFormType(CraftEntity.class,Entity.class));
            entityWorld = new FieldAccessor<>(NMSUtils.getFieldFormType(ServerEntity.class,ServerLevel.class));
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Field field = NMSUtils.getFieldFormType(SharedConstants.class,WorldVersion.class);
            field.setAccessible(true);
            DetectedVersion ver = (DetectedVersion) field.get(null);
            final Field getNameField = NMSUtils.getFieldFormType(DetectedVersion.class,String.class);
            getNameField.setAccessible(true);
            SERVER_VER = (String) getNameField.get(ver);
        }catch (IllegalAccessException | NoSuchFieldException e){
            e.printStackTrace();
            SERVER_VER = e.getMessage();
        }

        for (Method method : World.class.getMethods()) {
            if (method.getReturnType().equals(ProfilerFiller.class)){
                getProfilerFiller = method;
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
            ServerLevel ws = nmsWorldField.get(world);
            ServerChunkCache cps = chunkProvider.get(ws);
            ChunkMap pcm = playerChunkMap.get(cps);
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
            ServerChunkCache cps = chunkProvider.get(ws);
            ChunkMap pcm = playerChunkMap.get(cps);
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

    public static void clearEntityAI(org.bukkit.entity.Mob entity) {
        if (isNoAI(entity)) return;
        Mob ne = (Mob) getNmsEntity(entity);
        if (ne == null) return;
//        GoalSelector p = new GoalSelector(null);
        final GoalSelector goalSelector = createGoalSelector(ne);
        if (goalSelector == null) entity.remove();
        for (FieldAccessor<GoalSelector> pathfinderGoalSelector : pathfinderGoalSelectors) {
            pathfinderGoalSelector.set(ne,goalSelector);
        }
    }

    public static boolean isNoAI(org.bukkit.entity.Mob entity) {
        Mob ne = (Mob) getNmsEntity(entity);
        if (ne != null) for (FieldAccessor<GoalSelector> pathfinderGoalSelector : pathfinderGoalSelectors) {
            final GoalSelector goalSelector = pathfinderGoalSelector.get(ne);
            return goalSelector instanceof NoTickPathfinder;
        }
        return false;
    }

    public static GoalSelector createGoalSelector(net.minecraft.world.entity.Mob ne) {
        var world = entityWorld.get(ne);
        return pathCacheMap.computeIfAbsent(world,world1 -> {
            ProfilerFiller mp;
            try{
                mp = (ProfilerFiller) getProfilerFiller.invoke(world);
            }catch (IllegalAccessException | InvocationTargetException e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Supplier<ProfilerFiller> supplier = () -> mp;
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

    public static class NoTickPathfinder extends net.minecraft.world.entity.ai.goal.GoalSelector {
        public NoTickPathfinder(Supplier<ProfilerFiller> supplier) {
            super(supplier);
        }


        @Override
        // public void doTick()
        public void tick() {
        }
    }
}
