package com.yuong.media.player.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.yuong.media.player.widget.NetSpeedView;

import androidx.annotation.NonNull;

/**
 * 网速
 */
public class NetSpeed {
    private NetSpeedTimer netSpeedTimer;
    private Context context;
    private NetSpeedView speedView;

    public NetSpeed(Context context, NetSpeedView speedView) {
        this.context = context;
        this.speedView = speedView;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NetSpeedTimer.NET_SPEED_TIMER_DEFAULT:
                    long speed = (long) msg.obj;
                    speedView.showNetSpeed(speed);
                    break;
            }
        }
    };


    public void register() {
        speedView.showNetSpeed(0);
        netSpeedTimer = new NetSpeedTimer(context, handler);
        netSpeedTimer.startSpeedTimer();
    }

    public void unRegister() {
        speedView.dismissNetSpeed();
        if (netSpeedTimer != null) {
            netSpeedTimer.stopSpeedTimer();
            netSpeedTimer = null;
        }
    }
}
