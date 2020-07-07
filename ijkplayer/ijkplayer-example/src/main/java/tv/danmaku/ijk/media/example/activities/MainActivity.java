package tv.danmaku.ijk.media.example.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import tv.danmaku.ijk.media.example.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_ijkplayer_demo).setOnClickListener(this);
        findViewById(R.id.btn_player2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ijkplayer_demo:
                startActivity(new Intent(this, FileExplorerActivity.class));
                break;
            case R.id.btn_player:
                startActivity(new Intent(this, VideoActivity2.class));
                break;
            case R.id.btn_player2:
                startActivity(new Intent(this, AndroidMediaPlayerActivity.class));
                break;
        }
    }
}
