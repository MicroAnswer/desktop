package cn.microanswer.desktop.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.adapter.GridRecyclerViewAdapter;
import cn.microanswer.desktop.data.AppChangeReceiver;
import cn.microanswer.desktop.data.AppItem;
import cn.microanswer.desktop.data.AppItemLoader;
import cn.microanswer.desktop.other.Util;
import cn.microanswer.desktop.other.Utils;

public class MainActivity extends Activity implements AppItemView.OnOpenApp, AppItemLoader.OnLoadedListener {
    private RecyclerView recyclerView;
    private View emptyview;
    private AppItemView[] appItemViews;

    private GridLayoutManager layoutManager;
    private GridRecyclerViewAdapter adapter;

    private AppChangeReceiver changeReceiver;
    private IntentFilter intentFilter;

    private AppItemLoader appItemLoader;
    private ReLoadAppReceiver reLoadAppReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.parseColor("#55FFFFFF"));
            }
        }


        init();

        // 初始化广播接收器，用于重新加载app的接收
        reLoadAppReceiver = new ReLoadAppReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("reloadApp");
        registerReceiver(reLoadAppReceiver, intentFilter);
    }

    private void init() {
        initFastApp();

        emptyview = findViewById(R.id.emptyView);

        // 初始化RecyclerView
        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new GridLayoutManager(this, 5, GridLayoutManager.HORIZONTAL, false);
        adapter = new GridRecyclerViewAdapter(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 初始化广播接收器
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addDataScheme("package");

        changeReceiver = new AppChangeReceiver(this);
        registerReceiver(changeReceiver, intentFilter);

        int i = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == PackageManager.PERMISSION_GRANTED) {
            appItemLoader = new AppItemLoader(this);
            appItemLoader.execute(this);
        } else {
            String[] ps = new String[2];
            ps[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            ps[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                ps[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            ActivityCompat.requestPermissions(this, ps, 100);
        }
    }

    private void initFastApp() {
        // 初始化底部5个快捷方式
        LinearLayout linearLayout = findViewById(R.id.fastapp);
        int count = linearLayout.getChildCount();
        appItemViews = new AppItemView[count];
        for (int i = 0; i < count; i++) {
            appItemViews[i] = (AppItemView) linearLayout.getChildAt(i);
            appItemViews[i].setFastApp(true);
            appItemViews[i].setOnOpenApp(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                appItemLoader = new AppItemLoader(this);
                appItemLoader.execute(this);
            } else {
                Utils.UI.alert(this, "授权失败");
            }
        }
    }

    @Override
    public void doOpen(String pkg) {
        try {
            Util.open(this, pkg);
        } catch (Exception e) {
            Toast.makeText(this, "打不开：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addAppItem(AppItem appItem) {
        adapter.addAppItem(appItem);
    }

    public void remoAppItem(String packa) {
        adapter.removeAppItem(packa);
    }

    public void setAppItemTo(final int inde22x, final AppItem appItem) {
        Util.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject config = Util.getConfig(MainActivity.this);

                    JSONArray fastapps = null;

                    try {
                        fastapps = config.getJSONArray("fastapp");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fastapps == null) {
                        fastapps = new JSONArray();
                    }

                    boolean did = false;

                    for (int index = 0; index < fastapps.length(); index++) {
                        JSONObject o = fastapps.getJSONObject(index);
                        String pkg = o.getString("pkg");
                        int ind = o.getInt("index");
                        if (ind == inde22x) {
                            o.put("pkg", appItem.getPkg());
                            fastapps.put(index, o);
                            did = true;
                            break;
                        }
                    }
                    if (!did) {
                        JSONObject o = new JSONObject();
                        o.put("index", inde22x);
                        o.put("pkg", appItem.getPkg());
                        fastapps.put(o);
                    }
                    config.put("fastapp", fastapps);
                    Util.saveConfig(config);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appItemViews[inde22x].setAppItem(appItem);
                            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(changeReceiver);
            unregisterReceiver(reLoadAppReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforLoad() {
        emptyview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFastAppLoad(int inex, AppItem appItem) {
        appItemViews[inex].setAppItem(appItem);
    }

    @Override
    public void onAllAppLoad(ArrayList<AppItem> appItems) {
        adapter.setAppItems(appItems);
        emptyview.setVisibility(View.GONE);
        Toast.makeText(this, "加载完成", Toast.LENGTH_SHORT).show();
    }

    private class ReLoadAppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            appItemLoader = new AppItemLoader(MainActivity.this);
            appItemLoader.execute(context);
        }
    }
}
