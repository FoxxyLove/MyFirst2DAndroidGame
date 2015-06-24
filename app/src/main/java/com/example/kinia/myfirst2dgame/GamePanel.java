package com.example.kinia.myfirst2dgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;
    private int progressDenom = 20;




    public GamePanel(Context context)
    {
        super(context);

        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try
            {thread.setRunning(false);
                thread.join();
                retry = false;
            }
            catch(InterruptedException e){e.printStackTrace();}
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime=  System.nanoTime();
        missileStartTime = System.nanoTime();

        //start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            if(!player.getPlaying())
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            else
            {
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()
    {
        if(player.getPlaying())
        {

            bg.update();
            player.update();

            maxBorderHeight = 30+player.getScore()/progressDenom;
            if (maxBorderHeight > HEIGHT/4)maxBorderHeight = HEIGHT/4;

            minBorderHeight = 5+player.getScore()/progressDenom;

            for (int i = 0; i<botborder.size(); i++)
            {
                if (collision(botborder.get(i),player))
                    player.setPlaying(false);
            }

            for (int i = 0; i<topborder.size(); i++)
            {
                if (collision(topborder.get(i),player))
                    player.setPlaying(false);
            }

            this.updateTopBorder();
            this.updateBottomBorder();


            //add missiles on timer
            long missileElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if(missileElapsed >(2000 - player.getScore()/4))
            {
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile),WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {

                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+maxBorderHeight),45,15, player.getScore(),13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop every missile and check collision and remove
            for(int i = 0; i<missiles.size();i++)
            {
                //update missile
                missiles.get(i).update();

                if(collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120)
            {
                smoke.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else
        {
            newGameCreated = false;
            if (!newGameCreated)
            {
                newGame();
            }
        }
    }
    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);

        if(canvas!=null)
        {
            final int savedState = canvas.save();

            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
            //draw smokepuffs
            for(SmokePuff sp: smoke)
            {
                sp.draw(canvas);
            }
            //draw missiles
            for(Missile m: missiles)
            {
                m.draw(canvas);
            }

            canvas.restoreToCount(savedState);

            for (TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }
            for (BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }
        }
    }
    public void updateTopBorder()
    {
        if (player.getScore()%50==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),topborder.get(topborder.size()-1).getX()+20,0,(int)(rand.nextDouble()*(maxBorderHeight)+1)));
        }

        for (int i = 0; i < topborder.size(); i++)
        {
            topborder.get(i).update();
            if (topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                if (topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if (topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                if (topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()+1));
                }
                else
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }

    }

    public void updateBottomBorder()
    {
        if (player.getScore()%40==0)
        {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),botborder.get(botborder.size()-1).getX()+20,(int)(rand.nextDouble()*(maxBorderHeight)+(HEIGHT - maxBorderHeight))));
        }

        for (int i = 0; i<botborder.size(); i++)
        {
            botborder.get(i).update();
            if (botborder.get(i).getX()<-20) {
                botborder.remove(i);


                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1).getY() - 1));
                }
            }
        }
    }

    public void newGame()
    {
        botborder.clear();
        topborder.clear();
        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        for (int i = 0; i*20<WIDTH+40; i++)
        {
            if (i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,0,10));
            }
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,0,topborder.get(i-1).getHeight()+1));
            }
        }

        for (int i = 0; i*20<WIDTH+40; i++)
        {
            if (i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,HEIGHT-minBorderHeight));
            }
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),i*20,botborder.get(i-1).getY()-1));
            }
        }

        newGameCreated = true;

    }
}