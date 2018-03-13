package cn.microanswer.desktop.other;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

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
         *
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
         *
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

    /**
     * 文件相关工具类
     */
    public static class File {

        public static String readTxtFile(java.io.File file) throws Exception{
            return readTxtFile(file, "utf-8");
        }

        public static String readTxtFile(java.io.File file, String charset) throws Exception{
            FileInputStream fileInputStream = new FileInputStream(file);
            return Stream.readTxt(fileInputStream, charset);
        }

        public static String readTxtFile(String gfile, String charset) throws Exception{
            return readTxtFile(new java.io.File(gfile), charset);
        }

        public static void writeTxtFile(String s, java.io.File file) throws Exception{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(s);
            bufferedWriter.flush();
            writer.flush();
            fileOutputStream.flush();
        }
    }

    /**
     * 流相关工具类
     */
    public static class Stream {

        /**
         * 输入流copy到输出流
         *
         * @param inputStream
         * @param outputStream
         */
        public static void copy(InputStream inputStream, OutputStream outputStream) {


            BufferedInputStream inputStream1 = new BufferedInputStream(inputStream);
            BufferedOutputStream outputStream1 = new BufferedOutputStream(outputStream);

            try {
                byte[] data = new byte[1024];
                int datasize = 0;

                while ((datasize = inputStream1.read(data)) != -1) {
                    outputStream1.write(data, 0, datasize);
                }

                outputStream1.flush();
                outputStream.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream1 != null) {
                        inputStream1.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream1 != null) {
                        outputStream1.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

        }

        public static String readTxt(InputStream inputStream, String c) throws Exception{
            InputStreamReader reader = new InputStreamReader(inputStream, c);
            BufferedReader reader1 = new BufferedReader(reader);

            StringBuffer stringBuffer = new StringBuffer();

            char[] chars = new char[512];
            int size = 0;

            while ((size = reader1.read(chars))!=-1){
                stringBuffer.append(chars, 0, size);
            }

            reader.close();
            reader1.close();
            return stringBuffer.toString();
        }
    }

}
