package cn.microanswer.desktop.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Util;

/**
 * Created by Microanswer on 2018/3/7.
 */

public class SetActivity extends AppCompatActivity implements View.OnClickListener {
    private Cell changeBg, refresh, update, editConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.BLACK);
            }
        }

        init();
    }

    private void init() {
        changeBg = findViewById(R.id.changeBg);
        changeBg.setOnClickListener(this);

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(this);

        update = findViewById(R.id.update);
        update.setOnClickListener(this);

        editConfig = findViewById(R.id.editconfig);
        editConfig.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == changeBg) {
            Util.requestChangeBackground(this);
        } else if (v == refresh) {

            Intent intent = new Intent("reloadApp");
            sendBroadcast(intent);

        } else if (v == update) {

        } else if (v == editConfig) {
            startActivity(new Intent(this, EditConfigActivity.class));
        }
    }
}
