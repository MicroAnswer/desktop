package cn.microanswer.desktop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
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

public class MainActivity extends Activity implements AppItemView.OnOpenApp {
    private RecyclerView recyclerView;
    private View emptyview;
    private AppItemView[] appItemViews;

    private GridLayoutManager layoutManager;
    private GridRecyclerViewAdapter adapter;

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
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        }


        init();
    }

    private void init() {
        // 初始化底部5个快捷方式
        LinearLayout linearLayout = findViewById(R.id.fastapp);
        int count = linearLayout.getChildCount();
        appItemViews = new AppItemView[count];
        for (int i = 0; i < count; i++) {
            appItemViews[i] = (AppItemView) linearLayout.getChildAt(i);
        }

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
        } else{
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
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

    private class DataLoader extends AsyncTask<Void, Void, ArrayList<AppItem>> {

        @Override
        protected ArrayList<AppItem> doInBackground(Void... voids) {

            try {
                config = Util.getConfig(MainActivity.this);

                if (config != null) {
                    JSONObject c = (JSONObject) config;
                    config = c.getJSONArray("hide");
                }
            } catch (final Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                config = null;
            }

            ArrayList<AppItem> s = queryAppInfo(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String saf) {

                    if (config != null && config instanceof JSONArray) {
                        JSONArray hidelist = (JSONArray) config;
                        try {
                            JSONObject object = new JSONObject(saf);
                            String name = object.getString("name");
                            String pkg = object.getString("pkg");

                            for (int in = 0; in < hidelist.length(); in++) {
                                String sd = hidelist.getString(in);
                                if (name.equals(sd)) {
                                    return false;
                                }
                                if (pkg.equals(sd)) {
                                    return false;
                                }
                            }
                            return true;
                        } catch (Exception e) {
                            Log.i("MainActivity", e.getMessage());
                        }
                    }
                    return true;
                }
            });

            config = null;

            return s;
        }

        @Override
        protected void onPostExecute(ArrayList<AppItem> appItems) {
            super.onPostExecute(appItems);
            adapter.setAppItems(appItems);
            emptyview.setVisibility(View.GONE);
        }
    }


    // 获得所有启动Activity的信息，类似于Launch界面
    public ArrayList<AppItem> queryAppInfo(FilenameFilter filenameFilter) {
        ArrayList<AppItem> appItems = new ArrayList<>();

        AppItem set = new AppItem();
        set.setName("设置项");
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
