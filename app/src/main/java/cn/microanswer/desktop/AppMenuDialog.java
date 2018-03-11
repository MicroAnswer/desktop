package cn.microanswer.desktop;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by Micro on 2018-3-11.
 */

public class AppMenuDialog extends BottomSheetDialog implements View.OnClickListener {

    private AppItem appItem;
    private boolean isFastApp;

    private Cell open, uninstall, setsh;

    public AppMenuDialog(@NonNull Context context, boolean isFastApp) {
        super(context);
        this.isFastApp = isFastApp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_appmenu);

        open = findViewById(R.id.open);
        uninstall = findViewById(R.id.uninstall);
        setsh = findViewById(R.id.setsh);
        setsh.setOnClickListener(this);

        if (isFastApp) {
            setsh.setVisibility(View.GONE);
        }
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
        if ( v == setsh) {
            new SetFastAppDialog(getContext(), appItem).show();
            hide();
        } else {
            Toast.makeText(getContext(), "deving...", Toast.LENGTH_SHORT).show();
        }
    }
}
