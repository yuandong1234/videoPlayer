package com.yuong.player;

import android.os.Bundle;
import android.widget.TableLayout;

import com.yuong.player.media.AndroidMediaController;
import com.yuong.player.media.IjkVideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkMediaPlayerActivity extends AppCompatActivity {
    private IjkVideoView mVideoView;
    private TableLayout mHudView;

    private String mVideoPath = "http://saas-resources.52jiayundong.com/test/upload_file/file/20200703/20200703153425055871.mp4";

    private boolean mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ijkmedia_player);
        initView();
    }

    private void initView() {
        mVideoView = findViewById(R.id.video_view);
        mHudView = findViewById(R.id.hud_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        AndroidMediaController mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }


    @Override
    public void onBackPressed() {
        mBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mVideoView.stopPlayback();
//        mVideoView.release(true);
//        mVideoView.stopBackgroundPlay();
    }
}
