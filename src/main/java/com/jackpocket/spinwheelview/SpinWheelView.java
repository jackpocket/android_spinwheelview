package com.jackpocket.spinwheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SpinWheelView extends ImageView implements SpinnerTask.SpinTaskCallbacks {

    public static final float MIN_FORCE = 0;
    public static final float MAX_FORCE = 3;

    private static final float FORCE_ADJUSTMENT = 2;

    private float currentRotationDegrees = 0;

    private SpinnerTask spinnerTask;
    private boolean running = false;

    private Runnable spinCompletionListener;

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
    }

    public SpinWheelView setSpinCompletionListener(Runnable spinCompletionListener){
        this.spinCompletionListener = spinCompletionListener;
        return this;
    }

    /**
     * @param targetRotationDegrees ending rotation degrees
     */
    public void spinTo(float targetRotationDegrees){
        spinTo(targetRotationDegrees, 1.5f);
    }

    /**
     * @param targetRotationDegrees ending rotation degrees
     * @param force value between MIN_FORCE and MAX_FORCE
     */
    public void spinTo(float targetRotationDegrees, float force){
        spinTo(targetRotationDegrees, force, true);
    }

    /**
     * @param targetRotationDegrees ending rotation degrees
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
    public void update(final float currentRotationDegrees){
        post(new Runnable(){
            public void run(){
                SpinWheelView.this.currentRotationDegrees = currentRotationDegrees;

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

}
