package ru.samsung.itschool.funnybirds;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class DrawThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private Context context;

    //флаг для остановки потока
    private volatile boolean running = true;

    // птичка игрока
    private Sprite playerBird;
    // вражеская птичка
    private Sprite enemyBird;

    // ширина экрана
    protected int viewWidth;
    // высота экрана
    protected int viewHeight;

    // количество очков
    private int points = 0;

    // время сона потока
    private final int timerInterval = 30;

    public DrawThread(Context context, SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;
        // создание спрайтов
        createSprite();
    }

    // флаг остановки потока
    public void requestStop() {
        running = false;
    }

    // действия во стором потоке
    @Override
    public void run() {
        // пока флаг true
        while (running) {
            // получаем и блокируем конву в холдере
            Canvas canvas = surfaceHolder.lockCanvas();
            // если получили канву
            if (canvas != null) {
                    try {
                        // отрисовыввоем кадр
                        Paint p = new Paint();
                        canvas.drawARGB(250, 127, 199, 255);
                        // отрисовка птичек реализована в классе Sprite
                        playerBird.draw(canvas);
                        enemyBird.draw(canvas);

                        p.setAntiAlias(true);
                        p.setTextSize(55.0f);
                        p.setColor(Color.WHITE);
                        canvas.drawText(points + "", viewWidth - 200, 70, p);
                    } finally{
                        // возвращаем, разблокируем и отрисавываем канву на экране
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    // обновление положения птичек
                    update();
                }
                // приостанавливаем поток
                try {
                    Thread.sleep(timerInterval);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

        }
    }

    private void createSprite(){
        // получаем картинкусо спрайтами
        Bitmap b = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.player);
        // выделяем ширину и высоту одного спрайта
        int w = b.getWidth()/5;
        int h = b.getHeight()/3;
        // создаем прямоугольник размером со спрайт
        Rect firstFrame = new Rect(0, 0, w, h);
        // создаем объект птички с основными характеристиками
        playerBird = new Sprite(10, 1, 0, 100, firstFrame, b);
        // перебиаем все фреймы с картинки
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i ==0 && j == 0) {
                    continue;
                }
                if (i ==2 && j == 3) {
                    continue;
                }
                // добавляем фрейм (координаты прямоугольника на картинке)
                playerBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }

        b = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.enemy);
        w = b.getWidth()/5;
        h = b.getHeight()/3;
        firstFrame = new Rect(4*w, 0, 5*w, h);

        enemyBird = new Sprite(2000, 350, -200, 0, firstFrame, b);

        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {

                if (i ==0 && j == 4) {
                    continue;
                }

                if (i ==2 && j == 0) {
                    continue;
                }

                enemyBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }

        //t.start();
    }

    protected void update() {
        // перемещение птички по полю
        playerBird.update(timerInterval);
        enemyBird.update(timerInterval);
        //отражение нашей птички от стенок экрана
        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
            points--;
        }

        // пропуск вращеской птички
        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            // перемещение птички в начальное положение
            teleportEnemy();
            // изменение очков
            points +=10;
        }
        // столкновение с вражеской птички
        if (enemyBird.intersect(playerBird)) {
            teleportEnemy ();
            points -= 40;
        }
    }

    private void teleportEnemy () {
        // новое случайное положение на экране
        enemyBird.setX(viewWidth + Math.random() * 500);
        enemyBird.setY(Math.random() * (viewHeight - enemyBird.getFrameHeight()));
    }


    protected boolean touch(MotionEvent event){
        // получение клика на экран
        int eventAction = event.getAction();
        // действие нажатие на экран
        if (eventAction == MotionEvent.ACTION_DOWN) {
            // если над птичкой, то летим вверх
            if (event.getY() < playerBird.getBoundingBoxRect().top) {
                playerBird.setVy(-100);
                points--;
            }
            // если под птичкой, летим вниз
            else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                playerBird.setVy(100);
                points--;
            }
        }
        return false;
    }

}
