package com.yuong.media.player;

public interface IRenderView {
    int AR_ASPECT_FIT_PARENT = 0; // without clip
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_16_9_FIT_PARENT = 3;
    int AR_4_3_FIT_PARENT = 4;

    void setVideoSize(int videoWidth, int videoHeight);

    void setVideoAspectRatio(int videoSarNum, int videoSarDen);

    void setAspectRatio(int aspectRatio);

    void addRenderCallback(IRenderCallback callback);

    void removeRenderCallback();
}
