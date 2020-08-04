package com.yuong.media.player.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yuong.media.player.R;
import com.yuong.media.player.utils.DensityUtil;

import java.text.DecimalFormat;

public class NetSpeedView extends FrameLayout {
    private TextView textView;

    public NetSpeedView(Context context) {
        this(context, null);
    }

    public NetSpeedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        initProgressBar(context);
        initNetSpeed(context);
    }

    private void initProgressBar(Context context) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DensityUtil.dp2px(context, 100), DensityUtil.dp2px(context, 100), Gravity.CENTER);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_loading));
        progressBar.setLayoutParams(params);
        addView(progressBar);
    }

    private void initNetSpeed(Context context) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setLayoutParams(params);
        textView.setTextSize(12);
        textView.setText("0KB/S");
        addView(textView);
    }

    public void showNetSpeed(long speed) {
        this.setVisibility(VISIBLE);
        String speedDesc = convertData(speed);
        textView.setText(speedDesc);
    }

    public void dismissNetSpeed() {
        this.setVisibility(GONE);
        textView.setText("0KB/S");
    }

    private String convertData(long speed) {
        String result = "0";
        if (speed >= 1000) {
            double temp = speed / 1024d;
            DecimalFormat df = new DecimalFormat("#.00");
            result = df.format(temp) + "MB/S";
        } else {
            result = speed + "KB/S";
        }
        return result;
    }
}
