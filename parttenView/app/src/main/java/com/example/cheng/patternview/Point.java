package com.example.cheng.patternview;

/**
 * Created by cheng on 2017/10/9.
 */

public class Point {
    // 正常状态
    public static int STATE_NORMAL = 0;

    // 选中状态
    public static int STATE_CHECK = 1;

    // 出错状态
    public static int STATE_CHECK_ERROR = 2;

    // x坐标
    public float x;

    // y坐标
    public float y;

    public int state = 0;

    // 下标
    public int index = 0;

    // 从小圆，到大圆变化时的缩放比例
    public final static float ROUND_SCALE_FROM_VALUE = 0.3125f;

    public float mScale = ROUND_SCALE_FROM_VALUE;

    public Point() {

    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
