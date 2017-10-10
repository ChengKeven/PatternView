package com.example.cheng.patternview;
import android.content.Context;

import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.WindowManager;



/**

 * Created by baina on 17-9-25.

 */









public class Util {



    public static float DENSITY = Resources.getSystem().getDisplayMetrics().density; // 得到的结果是1.0，2.0, 3.0这种形式



    public static int dipToPixel(int dip) {

        if (dip < 0) {

            return -(int) (-dip * DENSITY + 0.5f);

        } else {

            return (int) (dip * DENSITY + 0.5f);

        }

    }





    public static int getScreenWidth(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();

        return width;

    }

    /**
     * ����ͼƬ
     *
     * @param bitmap
     * @param
     * @return
     */
    public static Bitmap zoom(Bitmap bitmap, float zf) {
        Matrix matrix = new Matrix();
        matrix.postScale(zf, zf);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    /**
     * ����ͼƬ
     *
     * @param bitmap
     * @param
     * @return
     */
    public static Bitmap zoom(Bitmap bitmap, float wf, float hf) {
        Matrix matrix = new Matrix();
        matrix.postScale(wf, hf);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    public static boolean checkInRound(float sx, float sy, float r, float x,
                                       float y) {
        return Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) < r;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2)
                + Math.abs(y1 - y2) * Math.abs(y1 - y2));
    }

    public static double pointTotoDegrees(double x, double y) {
        return Math.toDegrees(Math.atan2(x, y));
    }



}