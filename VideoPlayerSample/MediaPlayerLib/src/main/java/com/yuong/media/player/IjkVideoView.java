package com.yuong.media.player;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import com.yuong.media.player.network.NetSpeed;
import com.yuong.media.player.widget.LoadingView;
import com.yuong.media.player.widget.NetSpeedView;

import java.io.IOException;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkVideoView extends FrameLayout implements IRenderCallback {
    private final static String TAG = IjkVideoView.class.getSimpleName();

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private Context mContext;
    private SurfaceRenderView mRenderView;
    private LoadingView mLoadingView;
    private NetSpeedView mNetSpeedView;

    private int mCurrentAspectRatio = IRenderView.AR_ASPECT_FIT_PARENT;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private boolean mEnableMediaCodec;
    private IjkMediaPlayer mMediaPlayer = null;
    private SurfaceHolder mSurfaceHolder = null;

    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;

    private NetSpeed mNetSpeed;

    private Uri mUri;
    private Map<String, String> mHeaders;

    public IjkVideoView(Context context) {
        this(context, null);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
        initLoadingView();
        initNetSpeedView();
    }

    private void initVideoView(Context context) {
        mContext = context.getApplicationContext();
        mRenderView = new SurfaceRenderView(getContext());
        mRenderView.setAspectRatio(mCurrentAspectRatio);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mRenderView.setLayoutParams(lp);
        addView(mRenderView);
        mRenderView.addRenderCallback(this);

        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    private void initLoadingView() {
        mLoadingView = new LoadingView(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mLoadingView.setLayoutParams(lp);
        addView(mLoadingView);
        mLoadingView.dismiss();
    }

    private void initNetSpeedView() {
        mNetSpeedView = new NetSpeedView(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mNetSpeedView.setLayoutParams(lp);
        addView(mNetSpeedView);
        mNetSpeedView.dismissNetSpeed();
        mNetSpeed = new NetSpeed(getContext(), mNetSpeedView);
    }

    @Override
    public void onSurfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        Log.i(TAG, "############# onSurfaceCreated ##################");
        if (mMediaPlayer != null)
            bindSurfaceHolder(mMediaPlayer, holder);
        else
            openVideo();
    }

    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "############# onSurfaceChanged ################## width : " + width + " height : " + height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        boolean isValidState = (mTargetState == STATE_PLAYING);
        boolean hasValidSize = (mVideoWidth == width && mVideoHeight == height);
        if (mMediaPlayer != null && isValidState && hasValidSize) {
            start();
        }
    }

    @Override
    public void onSurfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "############# onSurfaceDestroyed ##################");
        mSurfaceHolder = null;
        if (mMediaPlayer != null)
            mMediaPlayer.setDisplay(null);
    }


    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        openVideo();
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }

        release();

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            mMediaPlayer = createPlayer();

            // TODO: create SubtitleController in MediaPlayer, but we need
            // a context for the subtitle renderers
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
//            mMediaPlayer.setOnInfoListener(mInfoListener);
//            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
//            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
//            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
//            mCurrentBufferPercentage = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            mLoadingView.show();
        } catch (IOException | IllegalArgumentException ex) {
            Log.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public IjkMediaPlayer createPlayer() {

        IjkMediaPlayer ijkMediaPlayer = null;
        if (mUri != null) {
            ijkMediaPlayer = new IjkMediaPlayer();
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
            if (mEnableMediaCodec) {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            } else {
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            }

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);// 需要准备好后自动播放
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);//设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);//视频帧处理不过来的时候丢弃一些帧达到同步的效果
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 8);// 跳过帧数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);//每处理一个packet之后刷新io上下文

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1);//是否开启预缓冲，一般直播项目会开启，达到秒开的效果，不过带来了播放丢帧卡顿的体验

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 100);//播放前的探测Size，默认是1M, 改小一点会出画面更快
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);//设置播放前的最大探测时间 （100未测试是否是最佳值
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1);//设置播放前的探测时间 1,达到首屏秒开效果


//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");//如果是rtsp协议，可以优先用tcp(默认是用udp)
        }

        return ijkMediaPlayer;
    }

    private void bindSurfaceHolder(IMediaPlayer mp, SurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }
        mp.setDisplay(holder);
    }

    public void start() {
        Log.i(TAG, "############# start ##################");
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        Log.i(TAG, "############# pause ##################");
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void stop() {
        Log.i(TAG, "############# stop ##################");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }


    public void release() {
        Log.i(TAG, "############# release ##################");
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public void initPlayer() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    public void destroyPlayer() {
        IjkMediaPlayer.native_profileEnd();
    }

    /**
     * int MEDIA_INFO_UNKNOWN = 1;//未知信息
     * int MEDIA_INFO_STARTED_AS_NEXT = 2;//播放下一条
     * int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频开始整备中，准备渲染
     * int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;//视频编码过于复杂，解码器无法足够快的解码
     * int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲中 开始缓冲
     * int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
     * int MEDIA_INFO_NETWORK_BANDWIDTH = 703;//网络带宽，网速方面
     * int MEDIA_INFO_BAD_INTERLEAVING = 800;//
     * int MEDIA_INFO_NOT_SEEKABLE = 801;//不可设置播放位置，直播方面
     * int MEDIA_INFO_METADATA_UPDATE = 802;//一组新的媒体的元数据用
     * int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
     * int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;//不支持字幕
     * int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;//字幕超时
     * int MEDIA_INFO_VIDEO_INTERRUPT= -10000;//数据连接中断，一般是视频源有问题或者数据格式不支持，比如音频不是AAC之类的
     * int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频方向改变，视频选择信息
     * int MEDIA_INFO_AUDIO_RENDERING_START = 10002;//音频开始整备中
     */

    /**
     * framework_err
     * int MEDIA_ERROR_UNKNOWN = 1;
     * int MEDIA_ERROR_SERVER_DIED = 100;//服务挂掉，视频中断，一般是视频源异常或者不支持的视频类型。
     * int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//播放错误（一般视频播放比较慢或视频本身有问题会引发）。
     * int MEDIA_ERROR_IO = -1004;//IO 本地文件或网络相关错误
     * int MEDIA_ERROR_MALFORMED = -1007;//音视频格式错误，demux或解码错误
     * int MEDIA_ERROR_UNSUPPORTED = -1010;//不支持的音视频格式
     * int MEDIA_ERROR_TIMED_OUT = -110;//数据超时
     * Error (-10000,0)
     */
    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            Log.i(TAG, "############# onError ##################");
            Log.e(TAG, "Error: " + framework_err + "," + impl_err);
            mLoadingView.dismiss();
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            switch (framework_err) {
                case IMediaPlayer.MEDIA_ERROR_IO:
                    Log.e(TAG, "本地文件或网络媒体错误");
                    break;
                case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    Log.e(TAG, "媒体播放超时");
                    break;
                case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.e(TAG, "媒体服务器挂起");
                    break;
                case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    Log.e(TAG, "媒体音视频格式不支持");
                    break;
                case IMediaPlayer.MEDIA_ERROR_MALFORMED:
                    Log.e(TAG, "媒体音视频格式错误");
                    break;
                case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                    Log.e(TAG, "媒体播放错误，不支持渐进式播放");
                    break;
                case IMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "未知错误");
                    break;
                default:
                    Log.e(TAG, "未知错误");
                    break;
            }

            switch (impl_err) {
                case IMediaPlayer.MEDIA_INFO_UNKNOWN:
                    Log.e(TAG, "媒体信息未知错误");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.e(TAG, "视频开始渲染，显示图像");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    Log.e(TAG, "视频编码过于复杂，解码器无法足够快的解码");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.e(TAG, "暂停播放等待缓冲更多数据");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.e(TAG, "视频缓冲结束恢复播放");
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.e(TAG, "媒体音视频交错出现错误");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.e(TAG, "媒体不支持Seek");
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.e(TAG, "一组新的媒体的元数据用");
                    break;
            }

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }

            if (getWindowToken() != null) {
                int messageId;
                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = R.string.VideoView_error_text_invalid_progressive_playback;
                } else {
                    messageId = R.string.VideoView_error_text_unknown;
                }

                new AlertDialog.Builder(getContext())
                        .setMessage(messageId)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                /* If we get here, there is no onError listener, so
                                 * at least inform them that the video is over.
                                 */
//                                        if (mOnCompletionListener != null) {
//                                            mOnCompletionListener.onCompletion(mMediaPlayer);
//                                        }
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            return true;
        }
    };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            Log.i(TAG, "############# onPrepared ##################");
            mCurrentState = STATE_PREPARED;
            mLoadingView.dismiss();

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            Log.i(TAG, "onPrepared video size: " + mVideoWidth + "/" + mVideoHeight);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            } else {
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        public void onCompletion(IMediaPlayer mp) {
            Log.i(TAG, "############# onCompletion ##################");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;

            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
            Log.e(TAG, "############# onInfo ################## ");
            Log.e(TAG, "onInfo  arg1 ：" + arg1);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, arg1, arg2);
            }
            switch (arg1) {
                case IMediaPlayer.MEDIA_INFO_OPEN_INPUT://媒体信息打开输入
                    Log.e(TAG, "媒体信息打开输入");
                    break;
                case IMediaPlayer.MEDIA_INFO_FIND_STREAM_INFO://媒体信息查找流信息
                    Log.e(TAG, "媒体信息查找流信息");
                    break;
                case IMediaPlayer.MEDIA_INFO_COMPONENT_OPEN://媒体信息组件打开
                    Log.e(TAG, "媒体信息组件打开");
                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_DECODED_START://媒体信息音频解码开始
                    Log.e(TAG, "媒体信息音频解码开始");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_DECODED_START://媒体信息视频解码开始
                    Log.e(TAG, "媒体信息视频解码开始");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING://视频编码过于复杂，解码器无法足够快的解码出帧
                    Log.e(TAG, "视频编码过于复杂，解码器无法足够快的解码出帧");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START://媒体视频开始渲染
                    Log.e(TAG, "媒体视频开始渲染");
                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://媒体音频开始渲染
                    Log.e(TAG, "媒体音频开始渲染");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START://暂停播放等待缓冲更多数据
                    Log.e(TAG, "暂停播放等待缓冲更多数据");
                    mNetSpeed.register();
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END://视频缓冲结束恢复播放
                    Log.e(TAG, "视频缓冲结束恢复播放");
                    mNetSpeed.unRegister();
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH://网速
                    Log.e(TAG, "网络速度: " + arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING://音视频错乱传输，视频跟音频不同步
                    Log.e(TAG, "音视频错乱传输，视频跟音频不同步");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE://不可移动帧，对于直播流
                    Log.e(TAG, "媒体不支持Seek");
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.e(TAG, "媒体信息元数据更新");
                    break;
                case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.e(TAG, "媒体不支持字幕信息");
                    break;
                case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT://渲染字幕时间过长
                    Log.e(TAG, "渲染字幕时间过长");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED://媒体信息视频旋转已更改
                    Log.e(TAG, "媒体信息视频旋转已更改");
                    break;
                case IMediaPlayer.MEDIA_INFO_UNKNOWN://未知的信息
                    Log.e(TAG, "未知的信息");
                    break;
            }
            return true;
        }
    };

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
            Log.i(TAG, "############# onVideoSizeChanged ##################");
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = mp.getVideoSarNum();
            mVideoSarDen = mp.getVideoSarDen();
            Log.e(TAG, "onVideoSizeChanged mVideoWidth :" + mVideoWidth + "  mVideoHeight: " + mVideoHeight);
            Log.e(TAG, "onVideoSizeChanged mVideoSarNum :" + mVideoSarNum + "  mVideoSarDen: " + mVideoSarDen);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                if (mRenderView != null) {
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                    mRenderView.setVideoAspectRatio(mVideoSarNum, mVideoSarDen);
                }
                requestLayout();
            }
        }
    };
}
