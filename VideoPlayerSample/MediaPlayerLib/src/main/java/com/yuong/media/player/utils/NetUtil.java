package com.yuong.media.player.utils;

import android.net.TrafficStats;

public class NetUtil {


    /**
     * 得到整个手机的流量值
     *
     * @param uid getApplicationInfo().uid
     * @return
     */
    public static long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }

    /**
     * 得到当前应用的流量值
     *
     * @param uid getApplicationInfo().uid
     * @return
     */
    public static long getUidRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getUidRxBytes(uid) / 1024);// 转为KB
    }
}
