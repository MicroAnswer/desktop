package cn.microanswer.desktop;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Microanswer on 2018/3/6.
 */

public class AppItemView extends LinearLayout implements View.OnClickListener, View.OnLongClickListener {

    private TextView name;
    private ImageView icon;
    private AppItem appItem;

    private boolean isFastApp = false;

    public AppItemView(Context context) {
        super(context);
        init(context, null);
    }

    public AppItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AppItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setFastApp(boolean fastApp) {
        isFastApp = fastApp;
    }

    public boolean isFastApp() {
        return isFastApp;
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(context, R.layout.view_appitem, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setMinimumWidth(Util.dp2px(context, 70f));
        int pf = Util.dp2px(getContext(), 20f);
        setPadding(pf, pf / 2, pf, pf / 2);
        setClickable(true);
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    public void bind(AppItem appItem, int position) {
        this.appItem = appItem;
        if (name == null) {
            name = (TextView) getChildAt(1);
        }
        if (icon == null) {
            icon = (ImageView) getChildAt(0);
        }
        if (!isFastApp()) {
            name.setVisibility(VISIBLE);
            name.setText(String.valueOf(appItem.getName()));
        }
        if (appItem.getIcon() != null) {
            icon.setImageDrawable(appItem.getIcon());
        }
    }

    @Override
    public void onClick(View v) {
        if (appItem != null && appItem.getPkg() != null) {

            if (appItem.getPkg().equals("sseett")) {
                getContext().startActivity(new Intent(getContext(), SetActivity.class));
                return;
            }

            if (onOpenApp != null) {
                onOpenApp.doOpen(appItem.getPkg());
            }

//            getContext().startActivity(appItem.getOpenIntent());
        } else {
            Toast.makeText(getContext(), "快捷方式未设置", Toast.LENGTH_SHORT).show();
        }
    }

    private OnOpenApp onOpenApp;

    public void setOnOpenApp(OnOpenApp onOpenApp) {
        this.onOpenApp = onOpenApp;
    }

    public OnOpenApp getOnOpenApp() {
        return onOpenApp;
    }

    @Override
    public boolean onLongClick(View v) {
        if (appItem == null) {
            Toast.makeText(getContext(), "快捷方式未设置", Toast.LENGTH_SHORT).show();
            return true;
        }
        AppMenuDialog appMenuDialog = new AppMenuDialog((MainActivity) getContext(), isFastApp);
        appMenuDialog.setAppItem(appItem);
        appMenuDialog.show();
        return true;
    }

    public void setAppItem(AppItem appItem) {
        this.bind(appItem, 0);
    }

    public interface OnOpenApp {
        void doOpen(String pkg);
    }
}
