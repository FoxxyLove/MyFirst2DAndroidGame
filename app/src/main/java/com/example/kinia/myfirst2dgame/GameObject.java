package com.example.kinia.myfirst2dgame;

import android.graphics.Rect;

public abstract class GameObject
{
    protected int x;
    protected int y;
    protected int dy;
    protected int dx;
    protected int width;
    protected int heigh;

    public void setX (int x)
    {
        this.x = x;
    }

    public void setY (int y)
    {
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int setY()
    {
        return y;
    }

    public int getHeigh()
    {
        return heigh;
    }

    public int getWidth()
    {
        return width;
    }

    public Rect getRectangle()
    {
        return new Rect(x, y, x+width, y+heigh);
    }
}