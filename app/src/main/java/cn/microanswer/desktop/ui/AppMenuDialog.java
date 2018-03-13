package cn.microanswer.desktop.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import cn.microanswer.desktop.data.AppItem;
import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Util;

/**
 * Created by Micro on 2018-3-11.
 */

public class AppMenuDialog extends BottomSheetDialog implements View.OnClickListener {
    private MainActivity activity;

    private AppItem appItem;
    private boolean isFastApp;

    private Cell open, uninstall, setsh, seeinfo;

    public AppMenuDialog(@NonNull MainActivity context, boolean isFastApp) {
        super(context);
        this.isFastApp = isFastApp;
        this.activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_appmenu);

        open = findViewById(R.id.open);
        uninstall = findViewById(R.id.uninstall);
        setsh = findViewById(R.id.setsh);
        seeinfo = findViewById(R.id.seeinfo);
        setsh.setOnClickListener(this);
        open.setOnClickListener(this);
        uninstall.setOnClickListener(this);
        seeinfo.setOnClickListener(this);

        if (isFastApp) {
            setsh.setVisibility(View.GONE);
        }
    }

    public void setAppItem(AppItem appItem) {
        this.appItem = appItem;
    }

    @Override
    protected void onStart() {

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);

        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if ("sseett".equals(appItem.getPkg())) {
            return;
        }

        if (v == setsh) {
            new SetFastAppDialog(activity, appItem).show();
        } else if (v == open) {
            try {
                Util.open(activity, appItem.getPkg());
            } catch (Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (v == uninstall) {
            Util.uninstallApk(activity, appItem.getPkg());
        } else if (seeinfo == v) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", appItem.getPkg(), null));
            getContext().startActivity(intent);
        } else {
            Toast.makeText(getContext(), "deving...", Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }
}
