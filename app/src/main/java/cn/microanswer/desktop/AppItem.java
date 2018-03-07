package cn.microanswer.desktop;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by Microanswer on 2018/3/6.
 */

public class AppItem {
    private String name;
    private Drawable icon;
    private String pkg;
    private Intent openIntent;

    public AppItem() {
    }

    public AppItem(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }

    public void setOpenIntent(Intent openIntent) {
        this.openIntent = openIntent;
    }

    public Intent getOpenIntent() {
        return openIntent;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getPkg() {
        return pkg;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setName(String name) {
        this.name = name;
    }
}
