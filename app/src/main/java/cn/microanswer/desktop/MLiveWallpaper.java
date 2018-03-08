package cn.microanswer.desktop;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/**
 * Created by Microanswer on 2018/3/6.
 */

public class MLiveWallpaper extends WallpaperService {


    class MyEng extends Engine implements Runnable, SensorEventListener {
        private SensorManager sensorManager;
        private Sensor sensor;

        private Context context;

        private Bitmap bitmap;
        private Rect dst, dst2;


        private boolean running = false; // 标记是否在绘制
        private Thread thread;

        private Paint paint;
        private boolean visvibe;

        public MyEng(Context context) {
            this.context = context;

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager != null) {
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            visvibe = true;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            paint.setTextSize(20f);
            ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

            running = true;
            thread = new Thread(this);
            thread.start();
        }

        private int r = 10, g = 10, b = 10;

        IntentFilter ifilter = null;


        private int moveTime = 60; // 动画时间120毫秒
        private int current = 0;

        private void draw(Canvas canvas, long delay) {
            canvas.drawColor(Color.rgb(r, g, b));

            if (bitmap != null) {
                if (dst == null) {
                    dst = new Rect();
                    dst.left = 0;
                    dst.top = 0;
                    dst.right = 1440;
                    dst.bottom = 2560;
                }

                if (dst2 == null) {
                    dst2 = new Rect();
                    dst2.left = dst.left;
                    dst2.right = dst.right;
                    dst2.top = dst.top;
                    dst2.bottom = dst.bottom;
                }

                dst2.left = Math.round(dst2.left + ((dst.left - dst2.left) * (current / (float) moveTime)));
                dst2.top = Math.round(dst2.top + ((dst.top - dst2.top) * (current / (float) moveTime)));
                dst2.right = dst2.left + 1440;
                dst2.bottom = dst2.top + 2560;
                canvas.drawBitmap(bitmap, null, dst2, paint);
                if (current >= moveTime) {
                    current = 0;
                } else {
                    current += delay;
                }
            }

            canvas.drawText("x=" + x, 100, 2560 - 60, paint);
            canvas.drawText("y=" + y, 100, 2560 - 40, paint);
            canvas.drawText("z=" + z, 100, 2560 - 20, paint);
            canvas.drawText("delay=" + delay, 100, 2560, paint);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            running = false;
            visvibe = false;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            this.visvibe = visible;
        }

        @Override
        public void run() {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
            synchronized (this) {
                SurfaceHolder surfaceHolder = getSurfaceHolder();
                long lastTime = System.currentTimeMillis();
                while (running) {
                    if (visvibe) {
                        Canvas canvas = surfaceHolder.lockCanvas();
                        if (canvas != null) {
                            draw(canvas, System.currentTimeMillis() - lastTime);
                            lastTime = System.currentTimeMillis();
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                    // 每秒 60 帧
                    SystemClock.sleep(1000 / 60);
                }
            }
        }


        private float x = -999, y = -999, z = -999;

        @Override
        public void onSensorChanged(SensorEvent e) {
            float nx = e.values[SensorManager.DATA_X];
            float ny = e.values[SensorManager.DATA_Y];
            float nz = e.values[SensorManager.DATA_Z];

            if (Math.abs(nx - x) > 5 && x != -999) {
                return; // 太突兀，不进行变化
            }
            if (Math.abs(ny - y) > 5 && y != -999) {
                return; // 太突兀，不进行变化
            }

            x = nx;
            y = ny;
            z = nz;

            nextXY(x, -y);
        }

        private void nextXY(float x, float y) {
            if (dst != null) {
                dst.left = Math.round(x * 8);
                dst.right = 1440 + dst.left;
                dst.top = Math.round(y * 8);
                dst.bottom = 2560 + dst.top;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    public Engine onCreateEngine() {
        return new MyEng(this);
    }
}
