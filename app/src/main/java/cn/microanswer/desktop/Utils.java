package cn.microanswer.desktop;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 工具类
 * Created by Microanswer on 2018/1/11.
 */

public class Utils {

    /**
     * 视图相关工具类
     */
    public static class UI {
        /**
         * 获取状态栏高度
         *
         * @param context
         * @return
         */
        public static int getStatusBarHeight(Context context) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        }

        /**
         * 获取导航栏高度
         *
         * @param context
         * @return
         */
        public static int getNavigationBarHeight(Context context) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            return resources.getDimensionPixelSize(resourceId);
        }

        /**
         * 将dp转化为px
         *
         * @param dp
         * @return
         */
        public static int dp2px(Context context, int dp) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
        }

        /**
         * 将px 转化为dp
         *
         * @param context
         * @param px
         * @return
         */
        public static int px2dp(Context context, int px) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }

        /**
         * 弹出警告框
         *
         * @param msg
         * @return
         */
        public static AlertDialog alert(Context context, String msg) {
            return alert(context, "提示", msg);
        }

        /**
         * 弹出警告框
         * @param context
         * @param title
         * @param msg
         * @return
         */
        public static AlertDialog alert(Context context, String title, String msg) {
            return alert(context, title, msg, null);
        }

        /**
         * 弹出警告框
         * @param context
         * @param title
         * @param msg
         * @param onClickListener
         * @return
         */
        public static AlertDialog alert(Context context, String title, String msg, DialogInterface.OnClickListener onClickListener) {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton("确定", onClickListener).create();
            alertDialog.show();
            return alertDialog;
        }
    }

}
