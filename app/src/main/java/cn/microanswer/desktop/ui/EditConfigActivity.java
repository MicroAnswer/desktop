package cn.microanswer.desktop.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Util;
import cn.microanswer.desktop.other.Utils;

/**
 * Created by Microanswer on 2018/3/20.
 */

public class EditConfigActivity extends AppCompatActivity implements TextWatcher , Runnable{
    private EditText editText;
    private boolean changed;

    private String configStr;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editconfig);
        editText = findViewById(R.id.edit);

        // 读取配置
        Util.runInThread(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        changed = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0,"保存");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Util.runInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    configStr = editText.getText().toString();
                    Util.saveConfig(new JSONObject(configStr));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditConfigActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            changed = false;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (changed) {
            Utils.UI.confirm(this,"配置已修改，您确定要退出吗？", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditConfigActivity.super.onBackPressed();
                }
            });
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void run() {
        try {
            JSONObject o =  Util.getConfig(this);
            configStr = o.toString(2);
        } catch (Exception e) {
            e.printStackTrace();
            configStr = "";
        }finally {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editText.setText(configStr);
                    editText.addTextChangedListener(EditConfigActivity.this);
                }
            });
        }
    }
}
