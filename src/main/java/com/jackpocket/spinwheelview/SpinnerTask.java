package com.jackpocket.spinwheelview;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class SpinnerTask implements Runnable {

    public interface SpinTaskCallbacks {
        public void update(float targetRotationDegrees);
        public void onSpinComplete();
    }

    private static final int UPDATE_RATE = 12;

    private SpinTaskCallbacks callbacks;
    private boolean canceled = false;

    private float startingRotation = 0;
    private float currentRotationDegrees = 0;
    private float targetRotationDifference = 0;

    private long startTime = 0;
    private float duration = 4950;
    private float percentCompleted = 0;

    private Interpolator interpolator = new DecelerateInterpolator();

    public SpinnerTask(float startingRotationDegrees, float targetRotationDegrees, float force, SpinTaskCallbacks callbacks){
        this.startingRotation = startingRotationDegrees % 360;
        this.currentRotationDegrees = startingRotation;
        this.targetRotationDifference = (360 * (int)(3 * force)) + targetRotationDegrees - startingRotation;
        this.callbacks = callbacks;
    }

    public SpinnerTask setDuration(long duration){
        this.duration = duration;
        return this;
    }

    @Override
    public void run() {
        this.startTime = System.currentTimeMillis();

        try{
            while(System.currentTimeMillis() - startTime < duration && !canceled){
                percentCompleted = interpolator.getInterpolation((System.currentTimeMillis() - startTime) / duration);
                currentRotationDegrees = startingRotation + (percentCompleted * targetRotationDifference);

                callbacks.update(currentRotationDegrees);

                Thread.sleep(UPDATE_RATE);
            }

            if(!canceled)
                callbacks.onSpinComplete();
        }
        catch(Exception e){ e.printStackTrace(); }
    }

    public void cancel(){
        this.canceled = true;
    }

}
