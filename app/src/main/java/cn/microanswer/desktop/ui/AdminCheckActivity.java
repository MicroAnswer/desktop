package cn.microanswer.desktop.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Util;

/**
 * 用于进行管理员验证的activity
 * Created by Micro on 2018-3-19.
 */

public class AdminCheckActivity extends AppCompatActivity implements Runnable {
    private PicLock picLock;
    private TextView hint;

    private ArrayList<PicLock.Dot> trues; // 正确图案

    private int count = 5; // 允许绘制图案的次数
    private int waitTime = 60 * 1000; // 5 次输入错误需要等待1分钟
    private long currentWaitTime;
    private int countBig; // 5 次为一组，全错则加1
    private SharedPreferences defaultSharedPreferences;
    private boolean paused;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admincheck);
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
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        picLock = findViewById(R.id.piclock);
        hint = findViewById(R.id.hint);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if ("openapp".equals(type)) {
            String pwd = intent.getStringExtra("pwd");
            String[] split = pwd.split(",");
            if (split.length > 2) {
                hint.setText("此应用已加密，请输入图案继续打开应用。");
                trues = new ArrayList<>();
                trues.add(picLock.newDot(split[0]));
                trues.add(picLock.newDot(split[1]));
                trues.add(picLock.newDot(split[2]));
                trues.add(picLock.newDot(split[3]));
                trues.add(picLock.newDot(split[4]));
            }
        }
        if (trues == null || trues.size() <= 2) {
            trues = new ArrayList<>();
            trues.add(picLock.newDot("00"));
            trues.add(picLock.newDot("10"));
            trues.add(picLock.newDot("01"));
            trues.add(picLock.newDot("11"));
            trues.add(picLock.newDot("21"));
        }
        paused = false;

        picLock.setOnResultListener(new PicLockListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (picLock != null) {
            picLock.setTryCount(0);
        }

        if (defaultSharedPreferences == null) {
            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }

        // 还有正在进行的倒计时，继续倒计时
        long waittime = defaultSharedPreferences.getLong("waittime", 0L);
        if (waittime > 0) {
            startTimeDown(waittime);
            if (picLock != null) {
                picLock.setDisabled(true);
            }
        }
    }

    @Override
    public void run() {
        startTimeDown(currentWaitTime);
    }

    private class PicLockListener extends PicLock.OnResultListener {

        private void inputErr() {
            if (picLock.getTryCount() <= 2) {
                hint.setText("图案绘制错误，请重新绘制图案。");
            } else if (picLock.getTryCount() < 5) {
                hint.setText(MessageFormat.format("您已经连续 {0} 次绘制图案错误，还可尝试 {1} 次。", picLock.getTryCount(), count - picLock.getTryCount()));
            } else {
                // 图案 5 次绘制都不正确
                picLock.setDisabled(true);
                countBig++;
                startTimeDown(countBig * 5 * waitTime);
            }
        }

        @Override
        public boolean onResult(ArrayList<PicLock.Dot> dots) {

            if (dots.size() != trues.size()) {
                inputErr();
                return false;
            }

            for (int i = 0; i < trues.size(); i++) {
                if (!trues.get(i).getData().equals(dots.get(i).getData())) {

                    // 图案错误
                    inputErr();
                    return false;
                }
            }

            Intent intent = getIntent();

            String type = intent.getStringExtra("type");
            if ("openapp".equals(type)) {
                String pkg = intent.getStringExtra("pkg");
                try {
                    Util.open(AdminCheckActivity.this, pkg);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(AdminCheckActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if ("openset".equals(type)) {
                startActivity(new Intent(AdminCheckActivity.this, SetActivity.class));
                finish();
            }

            return true;
        }
    }

    private void startTimeDown(long time) {
        if (paused) {
            paused = false;
            return;
        }
        String txt = MessageFormat.format("请等待 {0} 秒钟后再进行图案绘制", String.valueOf(time / 1000));
        currentWaitTime = time - 1000;
        if (time < 0) {
            picLock.setTryCount(0);
            picLock.setDisabled(false);
            txt = "请绘制管理员图案";
            hint.setText(txt);
            defaultSharedPreferences.edit().putLong("waittime", 0).commit();
            return;
        } else {
            defaultSharedPreferences.edit().putLong("waittime", currentWaitTime).commit();
            hint.setText(txt);
        }
        hint.postDelayed(this, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }
}
