package cn.microanswer.desktop;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.TypedValue;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by Microanswer on 2018/3/6.
 */

public class Util {
    public static int dp2px(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static AppItem getAppItem(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            CharSequence charSequence = applicationInfo.loadLabel(packageManager);
            Drawable drawable = applicationInfo.loadIcon(packageManager);
            AppItem appItem = new AppItem();
            appItem.setIcon(drawable);
            appItem.setName(charSequence.toString());
            appItem.setPkg(packageName);
            return appItem;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写在一个apk
     *
     * @param activity
     * @param packagename
     */
    public static void uninstallApk(MainActivity activity, String packagename) {
        //调用体统卸载程序，删除指定应用。
        Uri uri = Uri.fromParts("package", packagename, null);
        //也可以这样写：Uri uri=Uri.parse("package:"+packageName);
        Intent intentdel = new Intent(Intent.ACTION_DELETE, uri);
        activity.startActivity(intentdel);
    }


    public static void open(Activity context, String packagename) throws Exception {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            Toast.makeText(context, "包信息为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            context.startActivity(intent);
            context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public static void requestChangeBackground(Context context) {
        // 生成一个设置壁纸的请求
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, "chooser_wallpaper");
        //发送设置壁纸的请求
        context.startActivity(chooser);
    }

    /**
     * 获取配置文件
     *
     * @param context
     * @return
     */
    public static JSONObject getConfig(Context context) throws Exception {
        File configFileDir = new File(Environment.getExternalStorageDirectory(), ".desktop");
        if (!configFileDir.exists()) {
            if (!configFileDir.mkdirs()) {
                throw new Exception("配置文件夹创建失败");
            }
        }

        File configFile = new File(configFileDir, "config.cfg");

        if (!configFile.exists()) {
            // 配置文件不存在
            return new JSONObject();
        } else {
            // 配置文件存在;
            return new JSONObject(Utils.File.readTxtFile(configFile));
        }

    }

    public static void saveConfig(JSONObject config) throws Exception {
        File configFileDir = new File(Environment.getExternalStorageDirectory(), ".desktop");
        if (!configFileDir.exists()) {
            if (!configFileDir.mkdirs()) {
                throw new Exception("配置文件夹创建失败");
            }
        }

        File configFile = new File(configFileDir, "config.cfg");

        Utils.File.writeTxtFile(config.toString(), configFile);
    }
}
