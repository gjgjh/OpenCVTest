package com.example.opencvtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

// 自定义控件，在图片上绘制四边形
public class DrawImageView extends android.support.v7.widget.AppCompatImageView {
    private Paint currentPaint;
    private Path mPath;
    public ArrayList<Float> verticesX;          // 多边形顶点X坐标
    public ArrayList<Float> verticesY;          // 多边形顶点Y坐标

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setColor(0xFF00CC00);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(10);

        mPath=new Path();
        verticesX=new ArrayList<>();
        verticesY=new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath,currentPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (verticesX.size()==0){
                    mPath.moveTo(event.getX(), event.getY());
                    verticesX.add(event.getX());
                    verticesY.add(event.getY());
                }else if(verticesX.size()==1||verticesX.size()==2){
                    mPath.lineTo(event.getX(), event.getY());
                    verticesX.add(event.getX());
                    verticesY.add(event.getY());
                }else if (verticesX.size()==3){
                    mPath.lineTo(event.getX(), event.getY());
                    mPath.close();
                    verticesX.add(event.getX());
                    verticesY.add(event.getY());
                }
                break;
        }
        invalidate();

        return true;
    }

    // 清空绘制
    public void clearCanvas(){
        if (mPath!=null){
            mPath=new Path();
            verticesX=new ArrayList<>();
            verticesY=new ArrayList<>();
        }
        invalidate();
    }
}
