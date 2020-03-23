package cn.whiteg.memfree.utils;

import java.text.DecimalFormat;

public class CommonUtils {
    public static String tanByte(long l) {
        if (l < 0) return "NaN";
        final double k = 1024D;
        final DecimalFormat df = new DecimalFormat("#.00");
        if (l <= k){
            return df.format(l) + "B";
        }
        final double m = k * k;
        if (l <= m){
            return df.format(l / k) + "KB";
        }
        final double g = m * k;
        if (l <= g){
            return df.format(l / m) + "MB";
        }
        return df.format(l / g) + "GB";
    }

    public static String tanMintoh(long s) {
        final StringBuilder sb = new StringBuilder();
        if (s < 60){
            return sb.append(s).append("分钟").toString();
        }
        int m = 0;
        while (s >= 60) {
            s -= 60;
            m++;
        }
        sb.append(m).append("小时");
        if (s > 0) sb.append(s).append("分钟");
        return sb.toString();
    }
}
