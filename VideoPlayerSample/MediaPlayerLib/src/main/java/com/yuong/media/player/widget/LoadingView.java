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

public class LoadingView extends FrameLayout {
    private TextView textView;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        initProgressBar(context);
        initLoadingText(context);
    }

    private void initProgressBar(Context context) {
        LayoutParams params = new LayoutParams(DensityUtil.dp2px(context, 100), DensityUtil.dp2px(context, 100), Gravity.CENTER);
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress_loading));
        progressBar.setLayoutParams(params);
        addView(progressBar);
    }

    private void initLoadingText(Context context) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setLayoutParams(params);
        textView.setTextSize(12);
        textView.setText("加载中...");
        addView(textView);
    }

    public void show() {
        this.setVisibility(VISIBLE);
    }

    public void dismiss() {
        this.setVisibility(GONE);
    }
}
