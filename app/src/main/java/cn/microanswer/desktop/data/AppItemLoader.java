package cn.microanswer.desktop.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

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

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Util;

public class AppItemLoader extends AsyncTask<Context, Map<String, Object>, ArrayList<AppItem>> {

    private Object config;
    private OnLoadedListener onLoadedListener;

    public AppItemLoader(OnLoadedListener onLoadedListener) {
        this.onLoadedListener = onLoadedListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (onLoadedListener!=null) {
            onLoadedListener.beforLoad();
        }
    }

    @Override
    protected ArrayList<AppItem> doInBackground(Context... contexts) {
        if (contexts == null || contexts.length < 1) {
            return null;
        }

        Context context = contexts[0];

        try {
            config = Util.getConfig(context);

            if (config != null) {
                JSONObject c = (JSONObject) config;

                // 获取下方4个快捷方式内容
                try {
                    JSONArray fastapp = c.getJSONArray("fastapp");
                    if (fastapp != null && fastapp.length() > 0) {
                        for (int index = 0; index < fastapp.length(); index++) {
                            JSONObject jsonObject = fastapp.getJSONObject(index);
                            int i = jsonObject.getInt("index");
                            String pkg = jsonObject.getString("pkg");
                            AppItem appItem = Util.getAppItem(context, pkg);
                            if (appItem != null) {
                                TreeMap<String, Object> indval = new TreeMap<>();
                                indval.put("index", i);
                                indval.put("appitem", appItem);
                                publishProgress(indval);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                config = c.getJSONArray("hide");
            }
        } catch (final Exception e) {
            e.printStackTrace();
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
                        Log.i("AppItemLoader", e.getMessage());
                    }
                }
                return true;
            }
        }, context);

        config = null;

        return s;
    }

    // 获得所有启动Activity的信息，类似于Launch界面
    private ArrayList<AppItem> queryAppInfo(FilenameFilter filenameFilter, Context context) {
        ArrayList<AppItem> appItems = new ArrayList<>();

        AppItem set = new AppItem();
        set.setName("桌面选项");
        set.setIcon(new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_set)));
        set.setPkg("sseett");

        appItems.add(set);
        PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo reInfo : resolveInfos) {
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名

            String saf = pkgName.toLowerCase();
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
            // Log.i("size", "icon, class=" + icon.getClass().getSimpleName());

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

    @Override
    protected void onProgressUpdate(Map<String, Object>[] values) {
        super.onProgressUpdate(values);
        Map<String, Object> indval = values[0];
        int index = Integer.parseInt(indval.get("index").toString());
        AppItem appItem = (AppItem) indval.get("appitem");
        if (onLoadedListener != null) {
            onLoadedListener.onFastAppLoad(index, appItem);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<AppItem> appItems) {
        super.onPostExecute(appItems);
        if (onLoadedListener != null) {
            onLoadedListener.onAllAppLoad(appItems);
        }
    }

    public void setOnLoadedListener(OnLoadedListener onLoadedListener) {
        this.onLoadedListener = onLoadedListener;
    }

    public OnLoadedListener getOnLoadedListener() {
        return onLoadedListener;
    }

    public static interface OnLoadedListener {
        void beforLoad();

        void onFastAppLoad(int inex, AppItem appItem);

        void onAllAppLoad(ArrayList<AppItem> appItems);
    }
}