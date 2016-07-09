A simple library to create a spinning wheel View that allows you to spin smoothly to a specific angle.

![SpinWheelView Sample](https://github.com/jackpocket/android_spinwheelview/raw/master/spinwheelview.gif)

## Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.jackpocket:spinwheelview:1.0.1')
    }
```

## Usage

The SpinWheelView is just an ImageView that can rotate itself using a SpinnerTask. It should work with any image, but you may want to ensure they're square for aesthetics.

To use it, just tell it what degree to spin to. You can optionally supply a force value in the range of [0, 3], as well as make it rotate counter-clockwise instead. 

    ((SpinWheelView) findViewById(R.id.spin_wheel_view))
            .setSpinCompletionListener(() ->
                    Toast.makeText(this, "Spin Complete", Toast.LENGTH_SHORT)
                            .show())
            .spinTo(0, new Random().nextInt(4), true);

The spin methods are pretty self-explanatory:

    spinTo(float targetDegrees)
    spinTo(float targetDegrees, float force)
    spinTo(float targetDegrees, float force, boolean clockwise)

### Flick to Spin

With v1.0.1, the SpinWheelView can now be dragged and flung to land on whatever your desired target degrees is. 

To enable touch/drag/fling, you need to call *setSpinWithTouchEnabled(true)* on your SpinWheelView instance.

To set the target degrees on fling, you can preemptively call *setTargetDegreesOnFling(float)* or register a callback that can be run when the fling occurs to manually set it then. e.g.

    final SpinWheelView view = ((SpinWheelView) findViewById(R.id.spin_wheel_view))
            .setSpinWithTouchEnabled(true)
            .setTargetDegreesOnFling(45);

    view.setPreFlingCallback(() ->
            view.setTargetDegreesOnFling(new Random().nextInt(360));

