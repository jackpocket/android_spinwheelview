package com.jackpocket.spinwheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class SpinWheelView extends ImageView implements SpinnerTask.SpinTaskCallbacks,
        View.OnTouchListener {

    public static final float MIN_FORCE = 0;
    public static final float MAX_FORCE = 3;

    private static final float FORCE_ADJUSTMENT = 2;

    private float currentRotationDegrees = 0;

    private SpinnerTask spinnerTask;
    private boolean running = false;

    private Runnable spinCompletionListener;

    private boolean spinWithTouchEnabled = false;
    private float[] startingTouchPos = new float[]{ 0, 0 };
    private float[] currentTouchPos = new float[]{ 0, 0 };
    private float[] lastTouchPos = new float[]{ 0, 0 };

    private Runnable preFlingCallback;
    private int flingMaxDistance;
    private float flingTargetDegrees = 0;

    private int[] viewCenterPos;

    public SpinWheelView(Context context) {
        super(context);

        init();
    }

    public SpinWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SpinWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    protected void init(){
        setWillNotDraw(false);
        setOnTouchListener(this);

        this.flingMaxDistance = getPxFromDip(25);
    }

    public SpinWheelView setSpinCompletionListener(Runnable spinCompletionListener){
        this.spinCompletionListener = spinCompletionListener;
        return this;
    }

    public SpinWheelView setSpinWithTouchEnabled(boolean spinWithTouchEnabled){
        this.spinWithTouchEnabled = spinWithTouchEnabled;
        return this;
    }

    /**
     * @param flingTargetDegrees target degrees that a user's fling should cause the spin to land on
     */
    public SpinWheelView setTargetDegreesOnFling(float flingTargetDegrees){
        this.flingTargetDegrees = flingTargetDegrees % 360;
        return this;
    }

    /**
     * @param preFlingCallback a callback to be triggered immediately before triggering the spin (e.g. set the target degrees)
     */
    public SpinWheelView setPreFlingCallback(Runnable preFlingCallback){
        this.preFlingCallback = preFlingCallback;
        return this;
    }

    /**
     * @param targetRotationDegrees ending rotation degrees [0, 360]
     */
    public void spinTo(float targetRotationDegrees){
        spinTo(targetRotationDegrees, 1.5f);
    }

    /**
     * @param targetRotationDegrees ending rotation degrees [0, 360]
     * @param force value between MIN_FORCE and MAX_FORCE
     */
    public void spinTo(float targetRotationDegrees, float force){
        spinTo(targetRotationDegrees, force, true);
    }

    /**
     * @param targetRotationDegrees ending rotation degrees [0, 360]
     * @param force value between MIN_FORCE and MAX_FORCE
     * @param clockwise whether or not the rotation is clockwise
     */
    public void spinTo(float targetRotationDegrees, float force, boolean clockwise){
        if(running)
            return;

        stop();

        force = Math.min(force, MAX_FORCE);
        force = Math.max(force, MIN_FORCE);

        force += FORCE_ADJUSTMENT;

        spinnerTask = new SpinnerTask(currentRotationDegrees,
                targetRotationDegrees % 360,
                force * (clockwise ? 1 : -1),
                this);

        running = true;

        new Thread(spinnerTask)
                .start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(running || !spinWithTouchEnabled)
            return false;

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            onActionDown(event);
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE) onActionMove(event);
        else if(event.getAction() == MotionEvent.ACTION_UP) onActionUp(event);

        return false;
    }

    private void onActionDown(MotionEvent event){
        startingTouchPos = new float[]{ event.getRawX(), event.getRawY() };
        currentTouchPos = startingTouchPos;
        lastTouchPos = currentTouchPos;

        adjustAngleOnTouch();
    }

    private void onActionMove(MotionEvent event){
        lastTouchPos = currentTouchPos;
        currentTouchPos = new float[]{ event.getRawX(), event.getRawY() };

        adjustAngleOnTouch();
    }

    private void onActionUp(MotionEvent event){
        lastTouchPos = currentTouchPos;
        currentTouchPos = new float[]{ event.getRawX(), event.getRawY() };

        double distance = Math.sqrt(Math.pow(currentTouchPos[0] - lastTouchPos[0], 2) +
                Math.pow(currentTouchPos[1] - lastTouchPos[1], 2));

        float force = (float) ((distance / flingMaxDistance) * MAX_FORCE);

        float mX = currentTouchPos[0] - startingTouchPos[0];
        float mY = currentTouchPos[1] - startingTouchPos[1];

        // Pretty sure this isn't working correctly because of the flipped y-axis
        // but going to have to wait until next week to find out
        boolean isLeft = calculateCrossProduct(currentTouchPos, startingTouchPos, viewCenterPos) < 0;

        boolean clockwise;

        if(isLeft)
            clockwise = !((0 < mX && mY < 0) || (mX < 0 && mY < 0));
        else clockwise = !((mX < 0 && 0 < mY) || (0 < mX && 0 < mY));

        if(preFlingCallback != null)
            preFlingCallback.run();

        spinTo(flingTargetDegrees, force, clockwise);
    }

    public float calculateCrossProduct(float[] a, float[] b, int[] center){
        return (((b[0] - a[0]) * (center[1] - a[1])) - ((b[1] - a[1]) * (center[0] - a[0])));
    }

    private void adjustAngleOnTouch(){
        if(viewCenterPos == null){
            int[] locationInWindow = new int[2];

            getLocationInWindow(locationInWindow);

            viewCenterPos = new int[]{
                    locationInWindow[0] + (getWidth() / 2),
                    locationInWindow[1] + (getHeight() / 2)
            };
        }

        currentRotationDegrees = 180 - (float) Math.toDegrees(Math.atan2(currentTouchPos[0] - viewCenterPos[0],
                currentTouchPos[1] - viewCenterPos[1]));

        invalidate();
    }

    @Override
    public void update(final float currentRotationDegrees){
        post(new Runnable(){
            public void run(){
                SpinWheelView.this.currentRotationDegrees = currentRotationDegrees % 360;

                invalidate();
            }
        });
    }

    @Override
    public void onSpinComplete() {
        post(new Runnable(){
            public void run(){
                if(spinCompletionListener != null)
                    spinCompletionListener.run();
            }
        });

        stop();
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.save();

        canvas.rotate(currentRotationDegrees,
                canvas.getWidth() / 2,
                canvas.getHeight() / 2);

        super.onDraw(canvas);

        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();

        stop();
    }

    private void stop(){
        running = false;

        try{
            if(spinnerTask != null)
                spinnerTask.cancel();
        }
        catch(Exception e) { e.printStackTrace(); }
    }

    private int getPxFromDip(int dip) {
        return (int) (dip * getResources().getDisplayMetrics().density + 0.5f);
    }

}
