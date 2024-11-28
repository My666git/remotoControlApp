package com.example.remotecontrolapp.ui.dashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.example.remotecontrolapp.R;
import com.example.remotecontrolapp.tcp.SocketViewModel;

public class JoystickView extends View {

    private static final int JOYSTICK_RADIUS = 200; // 摇杆的半径
    private static final int INNER_RADIUS = 50; // 摇杆内圈的半径
    private static final float MAX_RADIUS = 200;
    private Point center; // 摇杆的中心点
    private Point currentPosition; // 摇杆的当前触摸位置
    private Bitmap joystickImage; // 摇杆的矩形图片资源
    private BitmapShader bitmapShader; // 用来绘制圆形的BitmapShader
    private Paint paint; // 绘制的画笔
    private boolean isTouched = false; // 是否被触摸
    private SocketViewModel socketViewModel;
    private float angle = 0;
    private final MutableLiveData<JoystickState> joystickPosition = new MutableLiveData<>(new JoystickState(0, 0));

    public JoystickView(Context context) {
        super(context);
        init(context);
    }

    // 获取 LiveData
    public MutableLiveData<JoystickState> getJoystickPosition() {
        return joystickPosition;
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        // 加载矩形摇杆图片
        joystickImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.joystick_button);
        joystickImage = Bitmap.createScaledBitmap(joystickImage, 10, 10, true);
        // 创建BitmapShader，并将矩形图片渲染成圆形
        bitmapShader = new BitmapShader(joystickImage, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // 默认设置摇杆中心点为View的中心
        center = new Point();
        currentPosition = new Point();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 将摇杆的中心设置为左上角位置，距离屏幕的左上角一定距离
        int offsetX = 100;  // 水平偏移
        int offsetY = 50;  // 垂直偏移
        center.set(offsetX + JOYSTICK_RADIUS, offsetY + JOYSTICK_RADIUS);  // 使摇杆位于左上角区域内
        currentPosition.set(center.x, center.y);  // 默认位置为中心
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 使用 center.x 和 center.y 来获取摇杆中心位置
        float joystickCenterX = center.x;
        float joystickCenterY = center.y;

        // 绘制摇杆背景（半透明黑色）
        paint.setColor(Color.argb(128, 0, 0, 0)); // 半透明黑色
        canvas.drawCircle(joystickCenterX, joystickCenterY, JOYSTICK_RADIUS, paint);

        // 绘制摇杆图片背景
        paint.setShader(bitmapShader);  // 使用BitmapShader来显示图片
        canvas.drawCircle(joystickCenterX, joystickCenterY, JOYSTICK_RADIUS, paint);
        paint.setShader(null);  // 重置shader

        // 如果触摸了摇杆，绘制白色圆形和图片
        if (isTouched) {
            // 绘制当前触摸位置的白色圆形（摇杆的内圈）
            paint.setColor(Color.WHITE);
            canvas.drawCircle(currentPosition.x, currentPosition.y, INNER_RADIUS, paint);

            // 绘制摇杆的图片，图片的圆形裁剪
            paint.setShader(bitmapShader);  // 使用BitmapShader来显示图片
            canvas.drawCircle(currentPosition.x, currentPosition.y, INNER_RADIUS, paint);
            paint.setShader(null);  // 重置shader
        } else {
            // 如果没有触摸时，仅绘制摇杆的背景，不绘制白色圆形
            // 绘制白色圆形
            paint.setColor(Color.WHITE);
            canvas.drawCircle(joystickCenterX, joystickCenterY, INNER_RADIUS, paint);

            // 绘制摇杆图片在中心位置
            paint.setShader(bitmapShader); // 使用BitmapShader来显示图片
            canvas.drawCircle(joystickCenterX, joystickCenterY, INNER_RADIUS, paint);
            paint.setShader(null);  // 重置shader
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        // 使用动态的摇杆中心位置
        int joystickCenterX = center.x;
        int joystickCenterY = center.y;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 判断触摸点是否在摇杆区域内
                if (isTouchInJoystick(x, y, joystickCenterX, joystickCenterY)) {
                    // 更新当前触摸位置
                    currentPosition.set(x, y);
                    isTouched = true;
                    // 计算触摸点与摇杆中心点的角度
                    calcAngle(x, y, joystickCenterX, joystickCenterY);

                    invalidate(); // 更新界面
                }else {
                    float distance = (float) Math.sqrt(Math.pow(x - joystickCenterX, 2) + Math.pow(y - joystickCenterY, 2));
                    if (distance > MAX_RADIUS) {
                        float ratio = MAX_RADIUS / distance;
                        x = (int) (joystickCenterX + (x - joystickCenterX) * ratio);
                        y = (int) (joystickCenterY + (y - joystickCenterY) * ratio);
                    }
                    currentPosition.set(x, y);
                    isTouched = true;
                    calcAngle(x, y, joystickCenterX, joystickCenterY);
                    invalidate(); // 更新界面
                }
                break;
            case MotionEvent.ACTION_UP:
                // 触摸结束，恢复到中心位置
                isTouched = false;
                currentPosition.set(joystickCenterX, joystickCenterY);
                // 更新LiveData中的值
                JoystickState currentState = joystickPosition.getValue();
                if (currentState != null) {
                    currentState.setAngle(0);
                    currentState.setDistance(0);
                    joystickPosition.setValue(currentState);
                }
                invalidate(); // 更新界面
                break;
        }

        return true;
    }

    private void calcAngle(int x, int y, int joystickCenterX, int joystickCenterY){
        // 计算触摸点与摇杆中心点的角度
        float dx = x - joystickCenterX;
        float dy = y - joystickCenterY;

        // 计算角度
        float angleInRadians = (float) Math.atan2(dy, dx);  // 计算弧度
        angle = (float) Math.toDegrees(angleInRadians);     // 转换为角度
        if (angle < 0) {
            angle += 360;
        }

        // 计算距离
        float distance = (float) Math.sqrt(dx * dx + dy * dy);  // 计算距离

        // 如果距离超过最大半径，则将其限制在最大半径内
        if (distance > MAX_RADIUS) {
            distance = MAX_RADIUS;
        }

        // 更新LiveData
        JoystickState currentState = joystickPosition.getValue();
        if (currentState != null) {
            currentState.setAngle(angle);
            currentState.setDistance(distance);
            joystickPosition.setValue(currentState);
        }
        //joystickPosition.setValue(new JoystickState(angle, distance));
    }


    private boolean isTouchInJoystick(int x, int y, int joystickCenterX, int joystickCenterY) {
        // 判断触摸点是否在摇杆的圆形区域内
        int dx = x - joystickCenterX;
        int dy = y - joystickCenterY;
        return (dx * dx + dy * dy <= JOYSTICK_RADIUS * JOYSTICK_RADIUS);
    }

    public float getAngle() {
        return angle;
    }
}
