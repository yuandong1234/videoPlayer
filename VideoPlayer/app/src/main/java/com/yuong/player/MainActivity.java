package com.yuong.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_play2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                startActivity(new Intent(this, IjkMediaPlayerActivity.class));
                break;
            case R.id.btn_play2:
                startActivity(new Intent(this, AndroidMediaPlayerActivity.class));
                break;
        }
    }
}
