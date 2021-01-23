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
import android.view.View;


public class GameView extends View{

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

    private Timer t = null;
    // через сколько будет срабатывать таймер
    private final int timerInterval = 30;

    public GameView(Context context) {
        super(context);

        // запуск таймера
        t = new Timer();
        // создание спрайтов
        createSprite();
    }

    private void createSprite(){

        // получаем картинкусо спрайтами
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.player);
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

        b = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
        w = b.getWidth()/5;
        h = b.getHeight()/3;
        firstFrame = new Rect(4*w, 0, 5*w, h);

        enemyBird = new Sprite(2000, 250, -200, 0, firstFrame, b);

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

        t.start();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // метод отрисовки на экране
        super.onDraw(canvas);
        Paint p = new Paint();

        canvas.drawARGB(250, 127, 199, 255);
        playerBird.draw(canvas);
        enemyBird.draw(canvas);

        p.setAntiAlias(true);
        p.setTextSize(55.0f);
        p.setColor(Color.WHITE);
        canvas.drawText(points + "", viewWidth - 200, 70, p);

    }

    protected void update () {
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
        // вызов метода onDraw - перерисовка канвы
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        return true;
    }


    private void teleportEnemy () {
        // новое случайное положение на экране
        enemyBird.setX(viewWidth + Math.random() * 500);
        enemyBird.setY(Math.random() * (viewHeight - enemyBird.getFrameHeight()));
    }

    // внутренний класс таймера
    class Timer extends CountDownTimer {

        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // каждый тик таймера вызов метода обновления
            update ();
        }

        @Override
        public void onFinish() {

        }
    }
}
