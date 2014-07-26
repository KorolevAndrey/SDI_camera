package com.camera.sdi.sdi_camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by sdi on 27.07.14.
 */
public class ScrollPowerView extends View {
    private int leftVal  = -1;
    private int rightVal = 1;
    private float currentVal;
    private Paint paint;

    public ScrollPowerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.BLUE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setStrokeWidth(height*3/4);

        canvas.drawColor(Color.BLACK);

        float tmp = currentVal > 0 ? rightVal : -leftVal;
        Log.d("ScrollPower", (currentVal/tmp) + "");
        //Log.d("ScrollPower", "lu: (" + (width/2) + ", " + (height/2) +") rd: ("+(width/2 + (currentVal/tmp * width/2))+ ", " + (height/2)+")");
        canvas.drawLine(
                width/2                             , height/2,
                width/2 + (currentVal/tmp * width/2), height/2,
                paint);

        super.onDraw(canvas);
    }

    /*
    * must be less than zero
    * */
    public void setLeftVal(int leftVal){
        this.leftVal = leftVal;
    }

    /*
    * must be more than zero
    * */
    public void setRightVal(int rightVal){
        this.rightVal = rightVal;
    }

    public void setBothVal(int leftVal, int rightVal){
        this.leftVal = leftVal; this.rightVal = rightVal;
    }

    public void setCurrentVal(int currentVal){
        Log.d("ScrollPower", "current value: " + currentVal);
        if (currentVal < leftVal)
            this.currentVal = leftVal;
        else if (currentVal > rightVal)
            this.currentVal = rightVal;
        else
            this.currentVal = currentVal;

        invalidate();
    }
}
