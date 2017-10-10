package com.example.cheng.patternview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;


import com.example.cheng.patternview.Point;
import com.example.cheng.patternview.R;
import com.example.cheng.patternview.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图形解锁控件
 */
public class PatternView extends View {
    private float w = 0;
    private float h = 0;

    //
    private boolean isInit = false;

    // 绘制点
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 绘制线
    private Paint mPaintToLine = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 9个点的坐标
    private Point[][] mPoints = new Point[3][3];

    private float r = 0;
    private List<Point> sPoints = new ArrayList<Point>();
    private boolean checking = false;

    // 点的初始状态图
    private Bitmap locus_round_original;

    // 点被按下后的图
    private Bitmap locus_round_click;

    // 普通的线
    private Bitmap locus_line;

    // 线的端点
    private Bitmap locus_line_semicircle;
    private long CLEAR_TIME = 0;

    // 最短密码位数
    private int passwordMinLength = 4;
    private boolean isTouch = true;
    private Matrix mMatrix = new Matrix();
    private Matrix mMatrix2 = new Matrix();

    // 线的alpha值
    private int lineAlpha = 110;

    public PatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PatternView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int minSize = Math.min(screenWidth, screenHeight);
        int dimention = Util.dipToPixel(40);
        minSize -= dimention;//居中展示 左右边距各留点空白
        setMeasuredDimension(minSize, minSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isInit) {
            init();//初始化 确定各个图的缩放比  确定图片
        }
        drawToCanvas(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 绘制
     *
     * @param canvas
     */
    private void drawToCanvas(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (p.state == Point.STATE_CHECK) {//将选中图片进行位移，
                    mMatrix2.reset();
                    mMatrix2.postTranslate(p.x - r, p.y - r);
                    mMatrix2.postScale(p.mScale, p.mScale, p.x, p.y);//围绕x y 做缩放
                    canvas.drawBitmap(locus_round_click, mMatrix2, mPaint);
                } else if (p.state == Point.STATE_CHECK_ERROR) {
                    // nothing to do
                } else {
                    canvas.drawBitmap(locus_round_original, p.x - r, p.y - r, mPaint);//在每个位置的左上角画图片
                }
            }
        }

        if (sPoints.size() > 0) {
            mPaintToLine.setAlpha(lineAlpha);
            Point tp = sPoints.get(0);

            for (int i = 1; i < sPoints.size(); i++) {//绘制已经划过的折线
                Point p = sPoints.get(i);
                drawLine(canvas, tp, p);
                tp = p;
            }

            if (this.movingNoPoint) {// 最后绘制当前的手指位置与前一个点。  //滑动过程中绘制这条线
                drawLine(canvas, tp, new Point((int) moveingX, (int) moveingY));
            }
        }
    }

    /**
     * 初始化
     *
     * @param
     */
    private void init() {

        // view的宽高
        w = this.getWidth();
        h = this.getHeight();

        float x = 0;
        float y = 0;

        if (w > h) {
            x = (w - h) / 2;
            w = h;
        } else {
            y = (h - w) / 2;
            h = w;
        }

        locus_round_original = BitmapFactory.decodeResource(this.getResources(), R.drawable.locus_round_original);
        locus_round_click = BitmapFactory.decodeResource(this.getResources(), R.drawable.locus_round_click);
        locus_line = BitmapFactory.decodeResource(this.getResources(), R.drawable.locus_line);
        locus_line_semicircle = BitmapFactory.decodeResource(this.getResources(), R.drawable.locus_line_semicircle);

        float canvasMinW = w;
        if (w > h) {
            canvasMinW = h;
        }

        // ?
        float roundMinW = canvasMinW / 8.0f * 2;//每个圆点最大的宽度

        float roundW = roundMinW / 2.f; //圆的半径

        // ?
        float deviation = canvasMinW % (8 * 2) / 2;
        x += deviation;
        x += deviation;

        if (locus_round_original.getWidth() > roundMinW) {
            float sf = roundMinW * 1.0f / locus_round_original.getWidth();
            locus_round_original = Util.zoom(locus_round_original, sf);
            locus_round_click = Util.zoom(locus_round_click, sf);
            locus_line = Util.zoom(locus_line, sf);
            locus_line_semicircle = Util.zoom(locus_line_semicircle, sf);
            roundW = locus_round_original.getWidth() / 2;
        }

        mPoints[0][0] = new Point(x + 0 + roundW, y + 0 + roundW);//x y 都等于0
        mPoints[0][1] = new Point(x + w / 2, y + 0 + roundW);   //  水平居中显示
        mPoints[0][2] = new Point(x + w - roundW, y + 0 + roundW);//靠近尾巴
        mPoints[1][0] = new Point(x + 0 + roundW, y + h / 2);//垂直居中显示
        mPoints[1][1] = new Point(x + w / 2, y + h / 2);//正中间显示
        mPoints[1][2] = new Point(x + w - roundW, y + h / 2);
        mPoints[2][0] = new Point(x + 0 + roundW, y + h - roundW);//靠近底部
        mPoints[2][1] = new Point(x + w / 2, y + h - roundW);
        mPoints[2][2] = new Point(x + w - roundW, y + h - roundW);

        int k = 0;
        for (Point[] ps : mPoints) {
            for (Point p : ps) {
                p.index = k;
                k++;
            }
        }
        r = locus_round_original.getHeight() / 2;// roundW;
        isInit = true;
    }

    /**
     * @param canvas
     * @param a
     * @param b
     */
    private void drawLine(Canvas canvas, Point a, Point b) {
        float ah = (float) Util.distance(a.x, a.y, b.x, b.y);
        float degrees = getDegrees(a, b);//获得a b两点之间的旋转角度
        canvas.rotate(degrees, a.x, a.y);//使得 a b两点旋转到水平 而且b点在a点右侧

        if (a.state == Point.STATE_CHECK_ERROR) {
            // nothing to do
        } else {
            mMatrix.setScale((ah - locus_line_semicircle.getWidth()) / locus_line.getWidth(), 1);//水平拉伸  根据两点之间的距离 纵向不拉伸
            mMatrix.postTranslate(a.x, a.y - locus_line.getHeight() / 2.0f);//图片的起始点移到a点
            canvas.drawBitmap(locus_line, mMatrix, mPaintToLine);
//            canvas.drawBitmap(locus_line_semicircle, a.x + ah - locus_line_semicircle.getWidth(),
//                    a.y - locus_line.getHeight() / 2.0f, mPaintToLine);//移动到
        }

        canvas.rotate(-degrees, a.x, a.y);
    }
    //获得a b两点之间的角度，起始角度 15分钟方向 顺时钟旋转
    public float getDegrees(Point a, Point b) {
        float ax = a.x;// a.index % 3;
        float ay = a.y;// a.index / 3;
        float bx = b.x;// b.index % 3;
        float by = b.y;// b.index / 3;
        float degrees = 0;
        if (bx == ax) {
            if (by > ay) {
                degrees = 90;
            } else if (by < ay) {
                degrees = 270;
            }
        } else if (by == ay) {
            if (bx > ax) {
                degrees = 0;
            } else if (bx < ax) {
                degrees = 180;
            }
        } else {
            if (bx > ax) {
                if (by > ay) {
                    degrees = 0;
                    degrees = degrees + switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
                } else if (by < ay) {
                    degrees = 360;
                    degrees = degrees - switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
                }

            } else if (bx < ax) {
                if (by > ay) {
                    degrees = 90;
                    degrees = degrees + switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                } else if (by < ay) {
                    degrees = 270;
                    degrees = degrees - switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                }

            }

        }
        return degrees;
    }

    /**
     * @param
     * @return
     */
    private float switchDegrees(float x, float y) {//x y所形成的角度
        return (float) Util.pointTotoDegrees(x, y);
    }

    /**
     * @param index
     * @return
     */
    public int[] getArrayIndex(int index) {
        int[] ai = new int[2];
        ai[0] = index / 3;
        ai[1] = index % 3;
        return ai;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    private Point checkSelectPoint(float x, float y) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {//判断x y是否在9个点的某一个位置。
                Point p = mPoints[i][j];

                if (null == p)
                    return null;

                if (Util.checkInRound(p.x, p.y, r, (int) x, (int) y)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * 重置
     */
    public void reset() {
        for (Point p : sPoints) {
            p.state = Point.STATE_NORMAL;
        }

        sPoints.clear();

        resetPointsScale();

        this.enableTouch();
    }

    /**
     * 重置点的缩放系数
     */
    private void resetPointsScale() {
        try {
            for (int i = 0; i < mPoints.length; i++) {
                for (int j = 0; j < mPoints[i].length; j++) {
                    mPoints[i][j].mScale = Point.ROUND_SCALE_FROM_VALUE;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param p
     * @return
     */
    private int crossPoint(Point p) {
        if (sPoints.contains(p)) {
            if (sPoints.size() > 2) {
                if (sPoints.get(sPoints.size() - 1).index != p.index) {
                    return 2;
                }
            }
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * @param point
     */
    private void addPoint(Point point) {
        this.sPoints.add(point);
    }

    /**
     * @param
     * @return
     */
    private String toPointString() {
        if (sPoints.size() >= passwordMinLength) {
            StringBuffer sf = new StringBuffer();
            for (Point p : sPoints) {
                sf.append(",");
                sf.append(p.index);
            }
            return sf.deleteCharAt(0).toString();
        } else {
            return "";
        }
    }

    boolean movingNoPoint = false;
    float moveingX, moveingY;

    /**
     * @param point
     */
    private void initAnimation(final Point point) {
        if (point.mScale > Point.ROUND_SCALE_FROM_VALUE)
            return;

        float[] floats = {Point.ROUND_SCALE_FROM_VALUE, 1.0f};
        ValueAnimator animator = ValueAnimator.ofFloat(floats);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (Float) animation.getAnimatedValue();
                point.mScale = scale;
                PatternView.this.invalidate();
            }
        });

        animator.setInterpolator(new OvershootInterpolator(4.0f));
        animator.setDuration(300);
        animator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isTouch) {
            return false;
        }

        movingNoPoint = false;

        float ex = event.getX();
        float ey = event.getY();
        boolean isFinish = false;
        boolean redraw = false;
        Point p = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (task != null) {
                    task.cancel();
                    task = null;
                    Log.d("task", "touch cancel()");
                }
                reset();
                p = checkSelectPoint(ex, ey);
                if (p != null) {//第一次落下的位置
                    checking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE://滑动过程中，不断更新点的位置,如果当前滑动的位置不是9个点的范围，更新moveingx moveingy
                if (checking) {
                    p = checkSelectPoint(ex, ey);
                    if (p == null) {
                        movingNoPoint = true;
                        moveingX = ex;
                        moveingY = ey;
                    }
                }
                break;
            case MotionEvent.ACTION_UP://滑动结束了
                p = checkSelectPoint(ex, ey);
                checking = false;
                isFinish = true;
                break;
        }
        if (!isFinish && checking && p != null) {
            initAnimation(p);
            int rk = crossPoint(p);
            if (rk == 2) {//2代表已经加入到之前划过的点
                // reset();`
                // checking = false;

                movingNoPoint = true;
                moveingX = ex;
                moveingY = ey;

                redraw = true;
            } else if (rk == 0) {//这里划入到了特定的点
                p.state = Point.STATE_CHECK;
                addPoint(p);
                redraw = true;
            }
        }

        if (redraw) {

        }
        if (isFinish) {//抬起来的时候
            if (this.sPoints.size() == 1) {
                this.reset();
            } else if (this.sPoints.size() < passwordMinLength && this.sPoints.size() > 0) {//如果选择的密码长度小于4 则取消spoints的内容
                clearPassword();
                resetPointsScale();
                Toast.makeText(this.getContext(), "too short",
                        Toast.LENGTH_SHORT).show();
            } else if (mCompleteListener != null) {// 已经符合预期了
                if (this.sPoints.size() >= passwordMinLength) {
                    this.disableTouch();
                    mCompleteListener.onComplete(toPointString());
                }
            }
        }
        this.postInvalidate();
        return true;
    }

    /**
     *
     */
    public void markError() {
        markError(CLEAR_TIME);
    }

    /**
     *
     */
    public void markError(final long time) {
        for (Point p : sPoints) {
            p.state = Point.STATE_CHECK_ERROR;
        }
        this.clearPassword(time);
    }

    /**
     *
     */
    public void enableTouch() {
        isTouch = true;
    }

    /**
     *
     */
    public void disableTouch() {
        isTouch = false;
    }

    private Timer timer = new Timer();
    private TimerTask task = null;

    /**
     *
     */
    public void clearPassword() {
        clearPassword(CLEAR_TIME);
    }

    /**
     *
     */
    public void clearPassword(final long time) {
        if (time > 1) {
            if (task != null) {
                task.cancel();
                Log.d("task", "clearPassword cancel()");
            }
            lineAlpha = 50;
            postInvalidate();
            task = new TimerTask() {
                public void run() {
                    reset();
                    postInvalidate();
                }
            };
            Log.d("task", "clearPassword schedule(" + time + ")");
            timer.schedule(task, time);
        } else {
            reset();
            postInvalidate();
        }
    }

    //
    private OnCompleteListener mCompleteListener;

    /**
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public interface OnCompleteListener {
        public void onComplete(String password);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // HardwareAccUtils.setLayerTypeAsHardware(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // HardwareAccUtils.setLayerTypeAsSoftware(this);
    }
}
