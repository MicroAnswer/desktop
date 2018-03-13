package cn.microanswer.desktop;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DataLoader extends AsyncTask<Void, Map<String, Object>, ArrayList<AppItem>> {
    private Context context;
    private JSONObject config;

    public DataLoader(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<AppItem> doInBackground(Void... voids) {

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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
                        Log.i("MainActivity", e.getMessage());
                    }
                }
                return true;
            }
        });

        config = null;

        return s;
    }

    @Override
    protected void onProgressUpdate(Map<String, Object>[] values) {
        super.onProgressUpdate(values);
        Map<String, Object> indval = values[0];
        int index = Integer.parseInt(indval.get("index").toString());
        AppItem appItem = (AppItem) indval.get("appitem");
        appItemViews[index].setAppItem(appItem);
    }

    @Override
    protected void onPostExecute(ArrayList<AppItem> appItems) {
        super.onPostExecute(appItems);
        // adapter.setAppItems(appItems);
        MainActivity.appItems = appItems;
        adapter.setAppItems(MainActivity.appItems);
        emptyview.setVisibility(View.GONE);
    }

    public void des() {
        context = null;
    }
}