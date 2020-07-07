package com.yuong.media.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yuong.media.player.utils.MeasureHelper;

/**
 * 视频渲染视图
 */
public class SurfaceRenderView extends SurfaceView implements IRenderView, SurfaceHolder.Callback {


    private MeasureHelper mMeasureHelper;
    private IRenderCallback mSurfaceCallback;

    public SurfaceRenderView(Context context) {
        this(context, null);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mMeasureHelper = new MeasureHelper();
        getHolder().addCallback(this);
        //noinspection deprecation
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            getHolder().setFixedSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void addRenderCallback(IRenderCallback callback) {

    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {

    }

    @Override
    public void setAspectRatio(int aspectRatio) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
