A simple library to create a spinning wheel View that allows you to spin smoothly to a specific angle.

## Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.jackpocket:spinwheelview:1.0.0')
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

#### TODO:
* Follow touch
* Swipe-to-spin

