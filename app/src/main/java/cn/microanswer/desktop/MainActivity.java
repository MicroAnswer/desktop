package cn.microanswer.desktop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends Activity implements AppItemView.OnOpenApp {
    private RecyclerView recyclerView;
    private View emptyview;
    private AppItemView[] appItemViews;

    private GridLayoutManager layoutManager;
    private GridRecyclerViewAdapter adapter;
    private static ArrayList<AppItem> appItems;

    private AppChangeReceiver changeReceiver;
    private IntentFilter intentFilter;

    private Object config;


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

        int i = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (i == PackageManager.PERMISSION_GRANTED) {
            new DataLoader().execute();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
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
                new DataLoader().execute();
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




    // 获得所有启动Activity的信息，类似于Launch界面
    public ArrayList<AppItem> queryAppInfo(FilenameFilter filenameFilter) {
        ArrayList<AppItem> appItems = new ArrayList<>();

        AppItem set = new AppItem();
        set.setName("桌面选项");
        set.setIcon(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_set)));
        set.setPkg("sseett");

        appItems.add(set);
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo reInfo : resolveInfos) {
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名

            String saf = pkgName.toLowerCase();
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
            Log.i("size", "icon, class=" + icon.getClass().getSimpleName());

            // 创建一个AppInfo对象，并赋值
            AppItem appInfo = new AppItem();
            appInfo.setName(appLabel);
            appInfo.setPkg(pkgName);
            appInfo.setIcon(icon);

            if (filenameFilter != null) {
                if (!filenameFilter.accept(null, appInfo.toString())) {
                    continue;
                }
            }
            appItems.add(appInfo); // 添加至列表中
        }

        Collections.sort(appItems, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem o1, AppItem o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
        return appItems;
    }

    public void addAppItem(AppItem appItem) {
        adapter.addAppItem(appItem);
    }

    public void remoAppItem(String packa) {
        adapter.removeAppItem(packa);
    }

    public void setAppItemTo(final int inde22x, final AppItem appItem) {

        new Thread(new Runnable() {
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
        }).start();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
