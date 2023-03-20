package cn.whiteg.memfree.runnable;

import cn.whiteg.memfree.MemFree;
import cn.whiteg.memfree.Setting;
import cn.whiteg.memfree.utils.CommonUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record AutoCleanLog(long maxSize,List<File> files,boolean dir2OneFile) implements Runnable {
    public static Set<File> ignoreFiles = new HashSet<>(0);

    public static AutoCleanLog deserialization(ConfigurationSection cs) {
        if (cs == null || !cs.getBoolean("Enable",false)){
            return null;
        }
        var logger = MemFree.logger;
        var maxsize = CommonUtils.toByteLength(cs.getString("MaxSize"));
        if (maxsize <= 0){
            logger.warning("无效配置,MaxSize不可为负数: " + maxsize);
            return null;
        }

        var strList = cs.getStringList("Files");
        if (strList.isEmpty()){
            return null;
        }

        var files = new ArrayList<File>(strList.size());
        for (String str : strList) {
            var file = new File(str.replace("/",File.separator)); //将路径里的'/'替换成当前系统的文件分隔符
            files.add(file);
        }

        var dir2OneFile = cs.getBoolean("Dir2OneFile");

        if (Setting.DEBUG){
            logger.info("已开启" + AutoCleanLog.class.getSimpleName());
            logger.info("文件大小筏值" + CommonUtils.tanByte(maxsize));
            logger.info("已监听的文件: ");
            for (File file : files) {
                logger.info((file.exists() ? (file.isDirectory() ? "文件夹" : "文件") : "不存在") + ": " + file);
            }
        }

        return new AutoCleanLog(maxsize,files,dir2OneFile);
    }

    public void loopFile(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()){
            //将文件夹内所有文件都当一个文件计算大小和删除
            if (dir2OneFile){
                File[] files = file.listFiles();
                if (files == null) return;
                long size = 0;
                for (File f : files)
                    if (f.isFile()){
                        size += f.length();
                    } else {
                        loopFile(f); //如果遇到文件夹那就进入文件夹,但不会继续当一个文件来统计
                    }
                if (size > maxSize){
                    MemFree.logger.warning(file + "文件夹大小已达到" + CommonUtils.tanByte(size) + (" 开始清理日志"));
                    for (File f : files) {
                        deleteAndCheck(f);
                    }
                }
            } else {
                //noinspection ConstantConditions
                for (File listFile : file.listFiles()) {
                    loopFile(listFile);
                }
            }
        } else {
            long size = file.length();
            if (size < maxSize) return;
            MemFree.logger.warning(file + "文件大小已达到" + CommonUtils.tanByte(size) + (" 开始清理日志"));
            deleteAndCheck(file);
        }
    }

    //删除后并检查是否删除成功
    public void deleteAndCheck(File file) {
        if (file.isDirectory() || ignoreFiles.contains(file)) return; //忽略无法删除的文件,可能是受系统保护
        if (file.delete()){
            MemFree.logger.warning("已删除文件");
        } else if (file.exists()){
            //如果文件没有被删除
            try{
                try (var out = new FileOutputStream(file,false)){
                    out.write(0);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            MemFree.logger.warning("已覆盖文件");
        }
        //二次验证
        if (file.exists() && file.length() > maxSize){
            MemFree.logger.warning("清理日志失败，已忽略文件: " + file);
            ignoreFiles.add(file);
        }
    }

    @Override
    public void run() {
        for (File file : files) {
            loopFile(file);
        }
    }
}
