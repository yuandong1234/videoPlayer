package com.yuong.media.player;

import android.view.SurfaceHolder;

public interface IRenderCallback {
    void onSurfaceCreated(SurfaceHolder holder);

    void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height);

    void onSurfaceDestroyed(SurfaceHolder holder);
}
