package cn.microanswer.desktop;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;

/**
 * Created by Micro on 2018-3-11.
 */

public class SetFastAppDialog extends Dialog {

    private Cell[] cells;
    private AppItem willSetAppItem;

    public SetFastAppDialog(@NonNull Context context, AppItem willSetAppItem) {
        super(context);
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
    }

    @Override
    protected void onStart() {

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);

        super.onStart();
    }
}
