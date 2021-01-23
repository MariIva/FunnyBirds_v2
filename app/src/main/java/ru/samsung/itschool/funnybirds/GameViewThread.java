package ru.samsung.itschool.funnybirds;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;



public class GameViewThread extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;

    public GameViewThread(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // запуск второго потока
        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // установка ширины и высоты экрана
        drawThread.viewWidth = width;
        drawThread.viewHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // остановка второго потока
        drawThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                // ожидание останвки второго потока
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        }
    }

    // отлов кликов на экран
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return drawThread.touch(event);
    }
}
