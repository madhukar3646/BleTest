package com.app.madhu.bletest.helperclasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

/**
 * Created by madhu on 3/27/2018.
 */

public class ScanAnimation extends View {

    private Context context;
    private int vwidth,vheight,strokewidth;
    private Paint paint;
    private int midx,midy,radius,tempradius;
    private Handler handler=new Handler();

    public ScanAnimation(Context context,int vwidth,int vheight)
    {
        super(context);
        this.context=context;
        this.vwidth=vwidth;
        this.vheight=vheight;
        this.strokewidth=vwidth/100;

        this.midx=vwidth/2;
        this.midy=vheight/2;
        this.radius=(vwidth/2)-strokewidth;
        tempradius=radius;

        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#808080"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokewidth);

        handler.postDelayed(thread,100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.parseColor("#209BDDFF"));
        canvas.drawCircle(midx,midy,radius,paint);
        paint.setColor(Color.parseColor("#209BDDFF"));
        canvas.drawCircle(midx,midy,tempradius,paint);
        if((tempradius/2)>0) {
            paint.setColor(Color.parseColor("#509BDDFF"));
            canvas.drawCircle(midx, midy, tempradius / 2, paint);
        }
        if((tempradius/3)>0) {
            paint.setColor(Color.parseColor("#309BDDFF"));
            canvas.drawCircle(midx, midy, tempradius / 3, paint);
        }
        if((tempradius/4)>0) {
            paint.setColor(Color.parseColor("#809BDDFF"));
            canvas.drawCircle(midx, midy, tempradius / 4, paint);
        }
    }

    Runnable thread=new Runnable() {
        @Override
        public void run() {

            if(tempradius>0)
             tempradius=tempradius-2;
            else
                tempradius=radius;
            invalidate();
            handler.postDelayed(thread,10);
        }
    };
}
