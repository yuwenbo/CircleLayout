package usage.ywb.personal.circlelayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import java.util.LinkedList;

import usage.ywb.personal.circlelayout.R;


/**
 * @author Kingdee.ywb
 * @version [ V.2.2.6  2018/9/11 ]
 */
public class CircleLayout extends ViewGroup implements NestedScrollingChild {

    /**
     * 角速度惯性衰减的加速度
     */
    private static final double SPEED_OFFSET = 0.05d;
    /**
     * 速度衰减的频率（每fps毫秒衰减一次）
     */
    private static final int FPS = 16;

    private static final int SPEED_SIZE = 5;

    /**
     * 控制惯性滑动
     */
    private Handler inertiaHandler = new Handler(Looper.getMainLooper());
    /**
     * 一个先进先出加速度列表
     * 保存手指滑动的最后{@link #SPEED_SIZE}个角速度
     * 轮盘惯性转动速度 = 列表中保存的所有速度的平均值
     */
    private LinkedList<Double> angleSpeeds = new LinkedList<>();

    private boolean isCreated = false;

    private int mLayoutWidth, mLayoutHeight;
    private int mChildWidth, mChildHeight;

    /**
     * 当前轮盘偏移角度
     */
    private double rotateAngle = 0;
    /**
     * 子控件中心点相对于布局中心点的距离（半径）
     */
    private float childRadius;
    /**
     * 内边界半径
     */
    private float insideRadius;
    /**
     * 外边界半径
     */
    private float outsideRadius;
    private Drawable centerDrawable;

    private float boundWidth;
    private int boundColor;
    private int circleColor;

    private Paint mPaint;

    /**
     * 瞬时偏移角度（瞬时起始角度）
     */
    private double startAngle;
    /**
     * 轮盘旋转的角速度
     */
    private double angleSpeed = 0;

    private GestureDetector gestureDetector;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    private NestedScrollingChildHelper nestedScrollingChildHelper;

    private boolean isCircleRing;

    private Runnable inertiaRunnable = new Runnable() {
        @Override
        public void run() {
            if (angleSpeed > Math.abs(SPEED_OFFSET) * 10) {
                rotateAngle += angleSpeed;
                requestLayoutChild();
                angleSpeed -= SPEED_OFFSET;
                inertiaHandler.postDelayed(this, FPS);
            } else if (angleSpeed < -Math.abs(SPEED_OFFSET) * 10) {
                rotateAngle += angleSpeed;
                requestLayoutChild();
                angleSpeed += SPEED_OFFSET;
                inertiaHandler.postDelayed(this, FPS);
            } else {
                int surplus = (int) rotateAngle % (360 / getChildCount());
                if (Math.abs(surplus) <= Math.abs(SPEED_OFFSET) * 10) {
                    rotateAngle = Math.floor(rotateAngle);
                    requestLayoutChild();
                    inertiaHandler.removeCallbacks(this);
                } else {
                    rotateAngle += angleSpeed;
                    requestLayoutChild();
                    inertiaHandler.postDelayed(this, FPS);
                }
            }
        }
    };

    public CircleLayout(Context context) {
        this(context, null);
    }

    public CircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout);
        for (int i = 0, len = a.length(); i < len; i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.CircleLayout_outsideRadius:
                    outsideRadius = a.getDimensionPixelSize(index, 0);
                    break;
                case R.styleable.CircleLayout_insideRadius:
                    insideRadius = a.getDimensionPixelSize(index, 0);
                    break;
                case R.styleable.CircleLayout_childWidth:
                    mChildWidth = a.getDimensionPixelSize(index, 0);
                    mChildHeight = mChildWidth;
                    break;
                case R.styleable.CircleLayout_boundWidth:
                    boundWidth = a.getDimensionPixelSize(index, 0);
                    break;
                case R.styleable.CircleLayout_boundColor:
                    boundColor = a.getColor(index, 0);
                    break;
                case R.styleable.CircleLayout_circleColor:
                    circleColor = a.getColor(index, 0);
                    break;
                case R.styleable.CircleLayout_centerDrawable:
                    centerDrawable = a.getDrawable(index);
                    break;
            }
        }
        a.recycle();

        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                int position = getSelectedPosition(e.getX(), e.getY());
                if (onItemClickListener != null) {
                    if (position >= 0 && position < getChildCount()) {
                        onItemClickListener.onItemClick(CircleLayout.this, getChildAt(position), position, 0);
                    } else {
                        if (isCircleCenter(e.getX(), e.getY())) {
                            onItemClickListener.onItemClick(CircleLayout.this, null, -1, 0);
                        }
                    }
                }
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                int position = getSelectedPosition(e.getX(), e.getY());
                if (position >= 0 && position < getChildCount() && onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(CircleLayout.this, getChildAt(position), position, 0);
                }
                super.onLongPress(e);
            }
        };
        gestureDetector = new GestureDetector(context, gestureListener);
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    private void addAngleSpeed(double angleSpeed) {
        if (angleSpeeds.size() >= SPEED_SIZE) {
            angleSpeeds.pollLast();
        }
        angleSpeeds.addFirst(angleSpeed);
    }

    /**
     * @return 快速滑动手指松开后的惯性角速度
     */
    private double getAngleSpeed() {
        double sum = 0;
        for (double angleSpeed : angleSpeeds) {
            sum += angleSpeed;
        }
        return sum / angleSpeeds.size();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && !isCreated) {
            isCreated = true;
            int mWidth = r - l;
            layout(l, t, r, mWidth + t);
        } else {
            if (outsideRadius == 0) {
                outsideRadius = (mLayoutWidth >> 1) - boundWidth / 2;
            }
            if (insideRadius == 0) {
                insideRadius = (int) (mLayoutWidth / 5) + boundWidth / 2;
            }
            childRadius = (outsideRadius + insideRadius) / 2;
            if (mChildWidth == 0) {
                mChildWidth = (int) ((outsideRadius - insideRadius) / 5 * 4);
                mChildHeight = mChildWidth;
            }
            if (centerDrawable != null) {
                int radius = (int) (insideRadius - boundWidth / 2);
                int centerX = mLayoutWidth / 2;
                int centerY = mLayoutHeight / 2;
                centerDrawable.setBounds(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            }
            requestLayoutChild();
        }
    }

    @Override
    @SuppressWarnings("SuspiciousNameCombination")
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mLayoutWidth = w < h ? w : h;
        mLayoutHeight = mLayoutWidth;
        requestLayout();
    }

    float startX, startY;
    float moveX, moveY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                if (!isCircleRing(startX, startY)) {
                    return true;
                }
                startAngle = getAngle(startX, startY);
                inertiaHandler.removeCallbacks(inertiaRunnable);
                angleSpeed = 0;
                nestedScrollingChildHelper.setNestedScrollingEnabled(true);
                nestedScrollingChildHelper.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                if (!isCircleRing) {
                    return true;
                }
                double moveAngle = getAngle(moveX, moveY);
                angleSpeed = moveAngle - startAngle;
                if (angleSpeed > 300) {
                    angleSpeed -= 360;
                } else if (angleSpeed < -300) {
                    angleSpeed += 360;
                }
                addAngleSpeed(angleSpeed);
                rotateAngle += angleSpeed;
                startAngle = moveAngle;
                requestLayoutChild();
                nestedScrollingChildHelper.dispatchNestedPreScroll((int) (moveX - startX), (int) (moveY - startY), null, null);
                startX = moveX;
                startY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                if (!isCircleRing) {
                    return true;
                }
                angleSpeed = getAngleSpeed();
                if (angleSpeeds.size() >= SPEED_SIZE && Math.abs(angleSpeed) > 5) {
                    inertiaHandler.post(inertiaRunnable);
                }
                angleSpeeds.clear();
                nestedScrollingChildHelper.dispatchNestedScroll(0, 0, 0, 0, null);
                nestedScrollingChildHelper.stopNestedScroll();
                nestedScrollingChildHelper.setNestedScrollingEnabled(false);
                break;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        float cx = (float) mLayoutWidth / 2;
        float cy = (float) mLayoutHeight / 2;

        mPaint.setXfermode(new Xfermode());

        mPaint.setStrokeWidth(outsideRadius - insideRadius);
        mPaint.setColor(circleColor);
        canvas.drawCircle(cx, cy, childRadius, mPaint);

        mPaint.setStrokeWidth(boundWidth);
        mPaint.setColor(boundColor);
        canvas.drawCircle(cx, cy, outsideRadius, mPaint);
        canvas.drawCircle(cx, cy, insideRadius, mPaint);

        super.dispatchDraw(canvas);

        if (centerDrawable != null) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawCircle(cx, cy, insideRadius - boundWidth / 2, mPaint);
            centerDrawable.draw(canvas);
        }

    }

    private int getSelectedPosition(float x, float y) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (x > child.getLeft() && x < child.getRight() && y > child.getTop() && y < child.getBottom()) {
                return i;
            }
        }
        return -1;
    }

    public boolean isCircleRing(float touchX, float touchY) {
        double hypot = Math.hypot((mLayoutWidth >> 1) - touchX, (mLayoutHeight >> 1) - touchY);
        isCircleRing = hypot > insideRadius && hypot < outsideRadius;
        return isCircleRing;
    }

    public boolean isCircleCenter(float touchX, float touchY) {
        double hypot = Math.hypot((mLayoutWidth >> 1) - touchX, (mLayoutHeight >> 1) - touchY);
        return hypot < insideRadius - boundWidth / 2;
    }

    /**
     * 请求布局子控件
     */
    private void requestLayoutChild() {
        float childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        float angleDelay = 360 / childCount;
        int cLeft, cTop;
        for (int i = 0; i < childCount; i++) {
            if (rotateAngle > 360) {
                rotateAngle -= 360;
            } else if (rotateAngle < 0) {
                rotateAngle += 360;
            }
            View childView = getChildAt(i);
            cLeft = Math.round((float) (childRadius * Math.cos(Math.toRadians(rotateAngle)) + (mLayoutWidth - mChildWidth) / 2));
            cTop = Math.round((float) (childRadius * Math.sin(Math.toRadians(rotateAngle)) + (mLayoutHeight - mChildHeight) / 2));
            childView.layout(cLeft, cTop, cLeft + mChildWidth, cTop + mChildHeight);
            rotateAngle += angleDelay;
        }
    }

    /**
     * 根据手指触点的位置计算偏移角度
     *
     * @param xTouch 触点相对布局的X坐标
     * @param yTouch 触点相对布局的Y坐标
     * @return 手指触电相对于布局中心点的偏移角度
     */
    private double getAngle(double xTouch, double yTouch) {
        //手指触电相对于控件中心点的位置
        double x = xTouch - (mLayoutWidth / 2d);
        double y = yTouch - (mLayoutHeight / 2d);
        switch (getQuadrant(x, y)) {
            case 1:
                return Math.toDegrees(Math.asin(y / Math.hypot(x, y)));
            case 2:
            case 3:
                return 180 - (Math.toDegrees(Math.asin(y / Math.hypot(x, y))));
            case 4:
                return 360 + Math.toDegrees(Math.asin(y / Math.hypot(x, y)));
            default:
                return 0;
        }
    }

    /**
     * 根据手指触点相对于布局中心点的坐标位置计算触点的象限
     */
    private int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public interface OnItemClickListener {

        void onItemClick(CircleLayout circleLayout, View child, int position, long id);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(CircleLayout circleLayout, View child, int position, long id);
    }

}
