package cn.microanswer.desktop.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import cn.microanswer.desktop.R;
import cn.microanswer.desktop.other.Utils;

/**
 * Created by Microanswer on 2018/3/19.
 */

public class PicLock extends View {

    private int row, col;
    private float dotRadio;
    private int dotColor, lineColor, errColor, trueColor, lineSize;
    private Dot[][] items;
    private Paint paint;
    private boolean isUserDrawing; // 标记用户是否正在绘制图案
    private boolean isRight; // 标记绘制的团是否正确
    private boolean canInput = true; // 标记是否可以进行绘制。 当用户输入错误一次，次字段设置为false， 等待500毫秒才可以进行下一次输入
    private int tryCount; // 用户绘制图案次数
    private ArrayList<Dot> userItems; // 用户绘制的图案包含的item；
    private OnResultListener onResultListener;

    private float ux, uy; // 用户当前手指滑动的位置

    public PicLock(Context context) {
        super(context);
        init(context, null);
    }

    public PicLock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        onResultListener = new OnResultListener();
    }

    public int getTryCount() {
        return tryCount;
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PicLock);
            row = typedArray.getInt(R.styleable.PicLock_row, 3);
            col = typedArray.getInt(R.styleable.PicLock_col, 3);
            dotColor = typedArray.getColor(R.styleable.PicLock_dot_color, Color.rgb(222, 222, 222));
            errColor = typedArray.getColor(R.styleable.PicLock_err_color, Color.rgb(222, 0, 0));
            trueColor = typedArray.getColor(R.styleable.PicLock_true_color, Color.rgb(0, 222, 0));
            lineColor = typedArray.getColor(R.styleable.PicLock_line_color, Color.rgb(222, 222, 222));
            dotRadio = typedArray.getDimensionPixelSize(R.styleable.PicLock_dot_radio, Utils.UI.dp2px(context, 5f));
            lineSize = typedArray.getDimensionPixelSize(R.styleable.PicLock_line_size, Utils.UI.dp2px(context, 2f));
            typedArray.recycle();
        }

        if (row <= 1) {
            row = 3;
        }
        if (col <= 1) {
            col = 3;
        }

        if (row > 10) {
            row = 10;
        }
        if (col > 10) {
            col = 10;
        }

        items = new Dot[row][col];
        for (int i = 0; i < row; i++) {
            for (int l = 0; l < col; l++) {
                items[i][l] = new Dot();
            }
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(lineSize);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 计算出每一个item的半径和坐标
        int x, y;

        // 初始化所有的item
        for (int index = 0; index < row; index++) {
            y = getPaddingTop() + Math.round(index * ((getMeasuredHeight() - (getPaddingBottom() + getPaddingTop())) / (float) (row - 1))); // y坐标
            for (int jndex = 0; jndex < col; jndex++) {
                x = getPaddingLeft() + Math.round(jndex * ((getMeasuredWidth() - (getPaddingLeft() + getPaddingRight())) / (float) (col - 1))); // x坐标
                Dot item = items[index][jndex];
                item.x = x;
                item.y = y;
                item.r = dotRadio * 2;
                item.drawr = dotRadio;
                item.color = dotColor;
                item.isUsed = false;
                item.data = onResultListener.getDotData(index, jndex);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制线条
        if (userItems != null) {
            for (int i = 0; i < userItems.size(); i++) {
                Dot item = userItems.get(i);
                Dot item2 = null;
                if (i + 1 < userItems.size()) {
                    item2 = userItems.get(i + 1);
                }

                if (item2 == null) {
                    // 最后一个还没有连接下一个，不用画线条了
                } else {
                    // 可以绘制线条
                    int color = paint.getColor();
                    if (tryCount > 0 && !isUserDrawing && !isRight && !canInput) {
                        // 绘制过1次以上且是错的，显示红色
                        paint.setColor(errColor);
                    } else if (tryCount > 0 && !isUserDrawing && isRight && !canInput) {
                        paint.setColor(trueColor);
                    } else {
                        paint.setColor(lineColor);
                    }
                    canvas.drawLine(item.x, item.y, item2.x, item2.y, paint);
                    paint.setColor(color);
                }
            }


            // 绘制最后一个连接点到用户手指的线条
            if (userItems.size() > 0 && isUserDrawing) {
                Dot item = userItems.get(userItems.size() - 1);

                int color = paint.getColor();
                if (tryCount > 0 && !isUserDrawing && !isRight && !canInput) {
                    paint.setColor(errColor);
                } else if (tryCount > 0 && !isUserDrawing && isRight && !canInput) {
                    paint.setColor(trueColor);
                } else
                    paint.setColor(lineColor);
                canvas.drawLine(item.x, item.y, ux, uy, paint);

                // 手指位置画一个小圆，看起来和谐一点
                canvas.drawCircle(ux, uy, lineSize, paint);

                paint.setColor(color);
            }
        }


        for (int index = 0; index < row; index++) {
            for (int jndex = 0; jndex < col; jndex++) {
                Dot item = items[index][jndex];
                item.draw(canvas, paint);
            }
        }
    }

    // 当某个item被用户绘制到了，则该方法被调用。
    private void onItemUsed(Dot item) {
        if (userItems == null) {
            userItems = new ArrayList<>();
        }
        if (!userItems.contains(item)) {
            userItems.add(item);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public OnResultListener getOnResultListener() {
        return onResultListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canInput || isDisabled()) { // 不允许输入或禁用的时候
            return false;
        }

        int action = event.getAction();

        float x = event.getX(0);
        float y = event.getY(0);
        ux = x;
        uy = y;

        if (action == MotionEvent.ACTION_CANCEL) {
            cancel();
        } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {

            for (int index = 0; index < row; index++) {
                for (int jndex = 0; jndex < col; jndex++) {
                    Dot item = items[index][jndex];
                    boolean s;
                    if (action == MotionEvent.ACTION_DOWN) {
                        s = item.onTouchDown(x, y);
                    } else {
                        s = item.onTouchMove(x, y);
                    }
                    if (s) {
                        return true;
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            canInput = false;
            tryCount++;
            isRight = doCheck();
            isUserDrawing = false;
            performClick();
            if (!canInput) {
                // 输入完成显示1秒钟状态
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancel();
                    }
                }, 1000);
            }
        }

        invalidate();
        return true;
    }

    private boolean doCheck() {
        // 检查图案正确性
        return onResultListener.onResult(userItems);
    }

    private void cancel() {
        // 取消绘制的图形

        for (int index = 0; index < row; index++) {
            for (int jndex = 0; jndex < col; jndex++) {
                Dot item = items[index][jndex];
                item.isUsed = false;
            }
        }
        if (userItems != null)
            userItems.clear();

        canInput = true;
        invalidate();

    }

    public Dot newDot(Object data) {
        return new Dot(data);
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    private boolean disabled = false;

    public boolean isDisabled() {
        return disabled;
    }

    public void setTryCount(int tryCount) {
        this.tryCount = tryCount;
    }

    public class Dot {
        private int x, y; // 坐标
        private float r, drawr; // 半径
        private int color; // 颜色
        private boolean isUsed; // 是否已经连接上了的
        private Object data; //

        public Dot() {
        }

        public Dot(Object data) {
            this.data = data;
        }

        /**
         * 该item所带的数据，<br/>
         * 可以为null,<br/>
         * 用于判断最终的图案是否正确的时候通常会用到。<br/>
         * 默认是坐标， 00 第一行第一列， 01 第一行第二列，以此推类<br/>
         *
         * @return
         */
        public Object getData() {
            return data;
        }

        /**
         * 绘制这个点
         *
         * @param canvas
         */
        private void draw(Canvas canvas, Paint paint) {
            int c = paint.getColor();
            if (tryCount > 0 && !isUserDrawing && !isRight && !canInput) {
                paint.setColor(errColor);
            } else if (tryCount > 0 && !isUserDrawing && isRight && !canInput) {
                paint.setColor(trueColor);
            } else
                paint.setColor(color);

            // 连接上的，将点点显示大一点
            canvas.drawCircle(x, y, (!isUsed) ? drawr : drawr * 1.3f, paint);
            paint.setColor(c);
        }

        private boolean onTouchDown(float px, float py) {
            float r = this.r * 2.5f;

            if (x - r < px && px < x + r && y - r < py && py < y + r) {
                // 在范围内
                if (!isUserDrawing) {
                    isUserDrawing = true;
                }
                if (!isUsed) {
                    isUsed = true;
                    onItemUsed(this);
                }
            }
            return false;
        }

        private boolean onTouchMove(float px, float py) {
            float r = this.r * 2.5f;
            if (x - r < px && px < x + r && y - r < py && py < y + r) {


                if (!isUsed) {
                    isUsed = true;
                    onItemUsed(this);
                }
            }

            return false;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Dot) {
                Dot i = (Dot) obj;
                return i.x == x && i.y == y;
            }
            return super.equals(obj);
        }
    }

    private static abstract class _OnResultListener {
        public abstract boolean onResult(ArrayList<Dot> dots);

        public Object getDotData(int rowIndex, int colIndex) {
            return rowIndex + "" + colIndex;
        }
    }

    public static class OnResultListener extends _OnResultListener {
        @Override
        public boolean onResult(ArrayList<Dot> dots) {
            return false;
        }
    }

}
