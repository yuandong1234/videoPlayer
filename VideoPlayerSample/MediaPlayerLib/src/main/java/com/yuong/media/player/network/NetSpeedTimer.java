package com.yuong.media.player.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.yuong.media.player.utils.NetUtil;

import java.util.Timer;
import java.util.TimerTask;

public class NetSpeedTimer {
    private long defaultDelay = 1000;
    private long defaultPeriod = 1000;
    private Context mContext;
    private Handler mHandler;
    private Timer mTimer;
    private SpeedTimerTask mSpeedTimerTask;

    public static final int NET_SPEED_TIMER_DEFAULT = 101010;

    public NetSpeedTimer(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
    }

    /**
     * 开启获取网速定时器
     */
    public void startSpeedTimer() {
        mTimer = new Timer();
        mSpeedTimerTask = new SpeedTimerTask(mContext, mHandler);
        mSpeedTimerTask.setRunning(true);
        mTimer.schedule(mSpeedTimerTask, defaultDelay, defaultPeriod);
    }

    /**
     * 关闭定时器
     */
    public void stopSpeedTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (null != mSpeedTimerTask) {
            mSpeedTimerTask.setRunning(false);
            mSpeedTimerTask.cancel();
            mSpeedTimerTask = null;
        }
    }


    private static class SpeedTimerTask extends TimerTask {
        private Context mContext;
        private Handler mHandler;
        private boolean mIsRunning;
        private long lastTotalRxBytes = 0;
        private long lastTimeStamp = 0;

        public void setRunning(boolean running) {
            mIsRunning = running;
        }

        public SpeedTimerTask(Context context, Handler handler) {
            this.mHandler = handler;
            this.mContext = context;
        }

        @Override
        public void run() {
            if (null != mHandler && mIsRunning) {
                Message obtainMessage = mHandler.obtainMessage();
                obtainMessage.what = NET_SPEED_TIMER_DEFAULT;
                obtainMessage.obj = getNetSpeed();
                mHandler.sendMessage(obtainMessage);
            }
        }

        public long getNetSpeed() {
            long nowTotalRxBytes = NetUtil.getUidRxBytes(mContext.getApplicationInfo().uid);
            long nowTimeStamp = System.currentTimeMillis();
            long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
            lastTimeStamp = nowTimeStamp;
            lastTotalRxBytes = nowTotalRxBytes;
            return speed;
        }
    }
}
