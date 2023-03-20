package cn.whiteg.memfree.utils;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class ShellKit {


    /**
     * 运行shell脚本
     *
     * @param shell 需要运行的shell脚本
     */
    public static void execShell(String shell) throws IOException {
        Runtime rt = Runtime.getRuntime();
        rt.exec(shell);

    }

    /**
     * 运行shell
     *
     * @param shStr 需要执行的shell
     * @return
     * @throws IOException 注:如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流.
     */
    public static void runShell(CommandSender sender,String shStr) throws Exception {
        Process process;
        process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",shStr},null,null);
        InputStreamReader ir = new InputStreamReader(process
                .getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        process.waitFor();
        while ((line = input.readLine()) != null) {
            sender.sendMessage(line);
        }
    }

    /**
     * 获得java进程id
     *
     * @return java进程id
     * @author lichengwu
     * @created 2012-1-18
     */
//    public static final String getPID() {
//        String pid = System.getProperty("pid");
//        if (pid == null){
//            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//            String processName = runtimeMXBean.getName();
//            if (processName.indexOf('@') != -1){
//                pid = processName.substring(0,processName.indexOf('@'));
//            } else {
//                pid = getPIDFromOS();
//            }
//            System.setProperty("pid",pid);
//        }
//        return pid;
//    }

    /**
     * 从操作系统获得pid
     * <p>
     * 对于windows，请参考:http://www.scheibli.com/projects/getpids/index.html
     *
     * @return
     * @author lichengwu
     * @created 2012-1-18
     */
//    private static String getPIDFromOS() {
//
//        String pid = null;
//
//        String[] cmd = null;
//
//        File tempFile = null;
//r
//        String osName = System.getProperty("os.name");
//        // 处理windows
//        if (osName.toLowerCase().contains("windows")){
//            FileInputStream fis = null;
//            FileOutputStream fos = null;
//
//            try{
//                // 创建临时getpids.exe文件
//                tempFile = File.createTempFile("getpids",".exe");
//                File getpids = new File(System.getResourcePath("getpids.exe"));
//                fis = new FileInputStream(getpids);
//                fos = new FileOutputStream(tempFile);
//                byte[] buf = new byte[1024];
//                while (fis.read(buf) != -1) {
//                    fos.write(buf);
//                }
//                // 获得临时getpids.exe文件路径作为命令
//                cmd = new String[]{tempFile.getAbsolutePath()};
//            }catch (FileNotFoundException e){
//                e.printStackTrace();
//            }catch (IOException e){
//                e.printStackTrace();
//            } finally {
//                if (tempFile != null){
//                    tempFile.deleteOnExit();
//                }
//                Closer.close(fis,fos);
//            }
//        }
//        // 处理非windows
//        else {
//            cmd = new String[]{"/bin/sh","-c","echo $$ $PPID"};
//        }
//        InputStream is = null;
//        ByteArrayOutputStream baos = null;
//        try{
//            byte[] buf = new byte[1024];
//            Process exec = Runtime.getRuntime().exec(cmd);
//            is = exec.getInputStream();
//            baos = new ByteArrayOutputStream();
//            while (is.read(buf) != -1) {
//                baos.write(buf);
//            }
//            String ppids = baos.toString();
//            // 对于windows参考：http://www.scheibli.com/projects/getpids/index.html
//            pid = ppids.split(" ")[1];
//        }catch (Exception e){
//            e.printStackTrace();
//        } finally {
//            if (tempFile != null){
//                tempFile.deleteOnExit();
//            }
//            Closer.close(is,baos);
//        }
//        return pid;
//    }

}
