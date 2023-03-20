package cn.whiteg.memfree;

import cn.whiteg.memfree.utils.CommonUtils;

public class MemfreeShutdownHook extends Thread {

    public MemfreeShutdownHook() {
    }

    @Override
    public void run() {
        long wait = Setting.shutdownHookWaitTime;
        if (wait <= 0){
            log("没有启用防堵塞");
            return;
        }
        var t = new Thread(() -> {
            log("开始关闭服务器");
            try{
                log("如果发生线程堵塞将会在" + CommonUtils.tanMintoh(wait) + "后强制关闭进程");
                Thread.sleep(wait);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            log("超出等待时间，强制退出进程");
            Runtime.getRuntime().halt(9);
        });
        t.setDaemon(true);
        t.start();
    }

    public void log(String str) {
        System.out.println(this.getClass().getSimpleName().concat(": ").concat(str));
    }
}
