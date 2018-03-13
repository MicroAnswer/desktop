package cn.microanswer.desktop.other;

import android.os.AsyncTask;

/**
 * Created by Micro on 2018-3-13.
 */

public class SimpalTask extends AsyncTask<Runnable, Void, Void> {
    @Override
    protected Void doInBackground(Runnable... objects) {
        if (objects != null && objects.length > 0) {
            for (Runnable r : objects) {
                r.run();
            }
        }
        return null;
    }
}
