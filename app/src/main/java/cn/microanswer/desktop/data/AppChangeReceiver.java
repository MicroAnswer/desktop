package cn.microanswer.desktop.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import cn.microanswer.desktop.ui.MainActivity;

/**
 * Created by Micro on 2018-3-11.
 */

public class AppChangeReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    public AppChangeReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getSchemeSpecificPart();
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            mainActivity.remoAppItem(packageName);
            Log.i(getClass().getSimpleName(), "卸载了：" + packageName);
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {

            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                CharSequence charSequence = applicationInfo.loadLabel(packageManager);
                Drawable drawable = applicationInfo.loadIcon(packageManager);
                AppItem appItem = new AppItem();
                appItem.setIcon(drawable);
                appItem.setName(charSequence.toString());
                appItem.setPkg(packageName);
                mainActivity.addAppItem(appItem);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(getClass().getSimpleName(), "安装了：" + packageName);
        }
    }
}
