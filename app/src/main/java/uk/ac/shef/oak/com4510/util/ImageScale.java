package uk.ac.shef.oak.com4510.util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * A class to scale the image when zooming in the ImageShow. Inspiration and help gained from the
 * below referenced locations.
 */
public class ImageScale implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private View imageView;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private boolean inScale;

    /**
     * Constructor for the image scale class. Initialises the scale gesturers and references
     * the view to be scaled.
     * @param context - Context given for the detectors
     * @param imageView - The imageview to be scaled
     */
    public ImageScale(Context context, ImageView imageView) {
        this.imageView = imageView;
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
    }

    /**
     * All credit for the inspiration for below given by Chris Stillwell, for his contribution
     * to the following stackoverflow question:
     * https://stackoverflow.com/questions/5790503/can-we-use-scale-gesture-detector-for-pinch-zoom-in-android
     *
     * Handles the positioning of the image in it's view. Takes the current scale values and works
     * out where the image is currently, and moves it to the position of the touch point.
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float newXPos = imageView.getX();
        float newYPos = imageView.getY();
        if(!inScale) {
            newXPos -= distanceX;
            newYPos -= distanceY;
        }

        WindowManager wm = (WindowManager) imageView.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point point = new Point();
        d.getSize(point);

        float detectedXPos = (imageView.getWidth() * scaleFactor - point.x) / 2;
        float detectedYPos = (imageView.getHeight() * scaleFactor - point.y) / 2;

        // Adjust the image position given the current scale factor, and the given touch point.
        if (newXPos > detectedXPos) newXPos = detectedXPos;
        else if (newXPos < -detectedXPos) newXPos = -detectedXPos;

        if (newYPos > detectedYPos) newYPos = detectedYPos;
        else if (newYPos < -detectedYPos) newYPos = -detectedYPos;

        // Set imageView to this new position
        imageView.setX(newXPos);
        imageView.setY(newYPos);

        return true;
    }

    /**
     * Simply passes the touch events on the detectors.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * All credit given to Suragch for the idea to use ScaleGestureDetectors in the following
     * stack overflow answer: https://stackoverflow.com/questions/10630373/android-image-view-pinch-zooming
     *
     * The zooming for the image. A simple scale setter for the image depending on the touch event.
     */
    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        scaleFactor *= scaleGestureDetector.getScaleFactor();
        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor);
        scaleFactor = (float)(int) (scaleFactor * 100) / 100;
        imageView.setScaleX(scaleFactor);
        imageView.setScaleY(scaleFactor);
        onScroll(null, null, 0, 0);
        return true;
    }

    /**
     * Reset the image to it's normal form when the user double taps on it.
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        imageView.setX(0);
        imageView.setY(0);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
        return true;
    }

    /**
     * Inherited Methods for the 3 implementations of this class.
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        inScale = false;
        onScroll(null, null, 0, 0);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        inScale = true;
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) { return true; }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) { return true; }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return true; }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) { return true; }

    @Override
    public boolean onDoubleTap(MotionEvent e) { return true; }
}
