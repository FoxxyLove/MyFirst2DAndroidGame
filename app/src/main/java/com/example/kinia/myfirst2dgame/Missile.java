package com.example.kinia.myfirst2dgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.animation.*;

import java.util.Random;

public class Missile extends GameObject
{
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Missile (Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {
        super.x=x;
        super.y=y;
        width=w;
        heigh=h;
        score=s;

        speed = 7 + (int) (rand.nextDouble()*score/30);

        if (speed>40) speed=40;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        for (int i=0; i<image.length; i++)
        {
            image[i]=Bitmap.createBitmap(spritesheet,0,i*heigh,width,heigh);
        }

        animation.setFrames(image);
        animation.setDelay(100-speed);

    }

    public void update()
    {
        x-=speed;
        animation.update();
    }

    public void draw (Canvas canvas)
    {
        try
        {
            canvas.drawBitmap(animation.getImage(),x,y,null);
        }
        catch (Exception e) {}
    }

    @Override
    public int getWidth()
    {
        return width-10;
    }
}
