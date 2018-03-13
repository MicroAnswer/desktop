package cn.microanswer.desktop.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import cn.microanswer.desktop.data.AppItem;
import cn.microanswer.desktop.R;

/**
 * Created by Micro on 2018-3-11.
 */

public class SetFastAppDialog extends Dialog implements View.OnClickListener {

    private Cell[] cells;
    private AppItem willSetAppItem;

    private MainActivity activity;

    public SetFastAppDialog(@NonNull MainActivity context, AppItem willSetAppItem) {
        super(context, R.style.Theme_AppCompat_DayNight_Dialog);
        this.activity = context;
        this.willSetAppItem = willSetAppItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_setfastapp);
        cells = new Cell[4];
        cells[0] = findViewById(R.id.setfast1);
        cells[1] = findViewById(R.id.setfast2);
        cells[2] = findViewById(R.id.setfast3);
        cells[3] = findViewById(R.id.setfast4);

        for (int index = 0; index < cells.length; index++) {
            cells[index].setOnClickListener(this);
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
        if (v == cells[0]) {
            activity.setAppItemTo(0, willSetAppItem);
        } else if (v == cells[1]) {
            activity.setAppItemTo(1, willSetAppItem);
        } else if (v == cells[2]) {
            activity.setAppItemTo(2, willSetAppItem);
        } else if (v == cells[3]) {
            activity.setAppItemTo(3, willSetAppItem);
        }
        // Toast.makeText(activity, "设置成功", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
