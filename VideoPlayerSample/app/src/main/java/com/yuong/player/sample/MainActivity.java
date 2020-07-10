package com.yuong.player.sample;

import android.os.Bundle;

import com.yuong.media.player.IjkVideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private IjkVideoView videoView;

    //    private String path = "http://saas-resources.52jiayundong.com/test/upload_file/file/20200624/20200624184542179942.mp4";
//    private String path = "http://saas-resources.52jiayundong.com/test/upload_file/file/20200703/20200703153425055871.mp4";
    private String path = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        videoView = findViewById(R.id.video_view);

        videoView.initPlayer();
        videoView.setVideoPath(path);
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stop();
        videoView.release();
        videoView.destroyPlayer();
    }
}
