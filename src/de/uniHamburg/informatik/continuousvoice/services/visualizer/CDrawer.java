package de.uniHamburg.informatik.continuousvoice.services.visualizer;

/**
 *  This is the drawer for the visualizer
 *  @author Pontus Holmberg (EndLessMind)
 *  Email: the_mr_hb@hotmail.com
 **/

import de.uniHamburg.informatik.continousvoice.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CDrawer extends SurfaceView implements SurfaceHolder.Callback {

    private Context mContext;
    private CDrawThread mDrawThread;
    private SurfaceHolder mHolder;

    private Boolean isCreated = false;

    /**
     * This is where you instance the drawer You relly don't need to care about
     * the parameters, they are set in the xml-layout
     * 
     * @param Apply
     *            the baseContext of you current acitivty
     * @param AttributeSet
     */
    public CDrawer(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        System.out.println("CDrawer()");
        mHolder = getHolder();
        mContext = paramContext;
        mHolder.addCallback(this);
        mDrawThread = new CDrawThread(mHolder, paramContext, new Handler() {
            public void handleMessage(Message paramMessage) {
            }
        });
        mDrawThread.setName("" + System.currentTimeMillis());
        setFocusable(true);
    }

    public Boolean GetDead2() {
        return mDrawThread.GetDead2();
    }

    /**
     * restarts the thread
     * 
     * @param Is
     *            the thread dead?
     */
    public void Restart(Boolean paramBoolean) {
        if (isCreated) {
            if (mDrawThread.GetDead2().booleanValue()) {
                mDrawThread.SetDead2(Boolean.valueOf(false));
                if ((!paramBoolean.booleanValue()) || (!mDrawThread.GetDead().booleanValue()))

                    mHolder = getHolder();
                mHolder.addCallback(this);
                System.out.println("Restart drawthread");
                mDrawThread = new CDrawThread(mHolder, mContext, new Handler() {
                    public void handleMessage(Message paramMessage) {
                    }
                });
                mDrawThread.setName("" + System.currentTimeMillis());
                mDrawThread.start();
                return;
            }
            Boolean No1, No2 = true;
            while (true) {
                while (No2 = true) {

                    try {
                        Thread.sleep(1000L);
                        System.out.println("Just chilling in Restart");
                        No2 = false;
                        mDrawThread.SetDead2(Boolean.valueOf(true));
                    } catch (InterruptedException localInterruptedException) {
                        localInterruptedException.printStackTrace();
                    }
                    return;
                }

                if (!mDrawThread.GetDead().booleanValue())
                    continue;
                mHolder = getHolder();
                mHolder.addCallback(this);
                System.out.println("Restart drawthread");
                mDrawThread = new CDrawThread(mHolder, mContext, new Handler() {
                    public void handleMessage(Message paramMessage) {
                    }
                });
                mDrawThread.setName("" + System.currentTimeMillis());
                mDrawThread.start();
                return;
            }
        }
    }

    public void SetRun(Boolean paramBoolean) {
        mDrawThread.setRun(paramBoolean);
    }

    public CDrawThread getThread() {
        return mDrawThread;
    }

    /**
     * Called when there's a change in the surface
     */
    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
        mDrawThread.setSurfaceSize(paramInt2, paramInt3);
    }

    /**
     * Creates the surface
     */
    public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
        System.out.println("surfaceCreated");
        if (mDrawThread.getRun().booleanValue()) {
            System.out.println("11111");
            isCreated = true;
            mDrawThread.start();

        }
        while (true) {
            System.out.println("22222");
            Restart(Boolean.valueOf(false));
            return;
        }
    }

    /**
     * Surface destroyd
     */
    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
        int i = 1;
        while (true) {
            if (i == 0)
                return;
            try {
                mDrawThread.join();
                i = 0;
            } catch (InterruptedException localInterruptedException) {
            }
        }
    }

    /**
     * The Drawer Thread, subclass to cDrawer class We want to keep most of this
     * process in a background thread, so the UI don't hang
     * 
     * @author Pontus Holmberg (EndLessMind) Email: the_mr_hb@hotmail.com
     */
    public class CDrawThread extends Thread {
        private Paint mBackPaint;
        private Bitmap mBackgroundImage;
        private short[] mBuffer;
        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;
        private Paint mLinePaint;
        private int mPaintCounter = 0;
        private SurfaceHolder mSurfaceHolder;
        private Boolean m_bDead = Boolean.valueOf(false);
        private Boolean m_bDead2 = Boolean.valueOf(true);
        private Boolean m_bRun = Boolean.valueOf(true);
        private Boolean m_bSleep = Boolean.valueOf(false);
        private int m_iScaler = 8;
        private int counter = 0;

        /**
         * Instance the Thread All the parameters i handled by the cDrawer class
         * 
         * @param paramContext
         * @param paramHandler
         * @param arg4
         */
        public CDrawThread(SurfaceHolder paramContext, Context paramHandler, Handler arg4) {
            mSurfaceHolder = paramContext;

            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setColor(getResources().getColor(R.color.foreground));

            mBackPaint = new Paint();
            mBackPaint.setAntiAlias(true);
            mBackPaint.setColor(getResources().getColor(R.color.main));

            mBuffer = new short[2048];
            mBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        /**
         * Allow you to change the size of the waveform displayed on the screen
         * Or scale of you so will
         * 
         * @return returns a new scale value
         */
        public int ChangeSensitivity() {
            m_iScaler = (2 + m_iScaler);
            if (m_iScaler > 20)
                m_iScaler = 1;
            return m_iScaler;
        }

        public Boolean GetDead() {
            return m_bDead;
        }

        public Boolean GetDead2() {
            return m_bDead2;
        }

        public Boolean GetSleep() {
            return m_bSleep;
        }

        public void SetDead2(Boolean paramBoolean) {
            m_bDead2 = paramBoolean;
        }

        public void SetSleeping(Boolean paramBoolean) {
            m_bSleep = paramBoolean;
        }

        /**
         * Calculate and draws the line
         * 
         * @param Canvas
         *            to draw on, handled by cDrawer class
         */
        public void doDraw(Canvas paramCanvas) {
            if (mCanvasHeight == 1)
                mCanvasHeight = paramCanvas.getHeight();
            paramCanvas.drawPaint(mBackPaint);
            /**
             * Set some base values as a starting point This could be considerd
             * as a part of the calculation process
             */
            int height = paramCanvas.getHeight();
            int BuffIndex = (mBuffer.length / 2 - paramCanvas.getWidth()) / 2;
            int width = paramCanvas.getWidth();
            int mBuffIndex = BuffIndex;
            int scale = height / m_iScaler;
            int StratX = 0;
            if (StratX >= width) {
                paramCanvas.save();
                return;
            }
            int cu1 = 0;
            /**
             * Here is where the real calculations is taken in to action In this
             * while loop, we calculate the start and stop points for both X and
             * Y
             * 
             * The line is then drawer to the canvas with drawLine method
             */
            while (StratX < width - 1) {

                int StartBaseY = mBuffer[(mBuffIndex - 1)] / scale;

                int StopBaseY = mBuffer[mBuffIndex] / scale;
                if (StartBaseY > height / 2) {
                    StartBaseY = 2 + height / 2;
                    int checkSize = height / 2;
                    if (StopBaseY <= checkSize)
                        return;
                    StopBaseY = 2 + height / 2;
                }

                int StartY = StartBaseY + height / 2;
                int StopY = StopBaseY + height / 2;
                paramCanvas.drawLine(StratX, StartY, StratX + 1, StopY, mLinePaint);
                cu1++;
                mBuffIndex++;
                StratX++;
                int checkSize_again = -1 * (height / 2);
                if (StopBaseY >= checkSize_again)
                    continue;
                StopBaseY = -2 + -1 * (height / 2);
            }
        }

        public Boolean getRun() {
            return m_bRun;
        }

        /**
         * Updated the Surface and redraws the new audio-data
         */
        public void run() {
            while (true) {
                if (!m_bRun.booleanValue()) {
                    m_bDead = Boolean.valueOf(true);
                    m_bDead2 = Boolean.valueOf(true);
                    System.out.println("Goodbye Drawthread");
                    return;
                }
                Canvas localCanvas = null;
                try {
                    localCanvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (localCanvas != null)
                            doDraw(localCanvas);

                    }
                } finally {
                    if (localCanvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(localCanvas);
                }
            }
        }

        public void setBuffer(short[] paramArrayOfShort) {
            synchronized (mBuffer) {
                mBuffer = paramArrayOfShort;
                return;
            }
        }

        public void setRun(Boolean paramBoolean) {
            m_bRun = paramBoolean;
        }

        public void setSurfaceSize(int paramInt1, int paramInt2) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = paramInt1;
                mCanvasHeight = paramInt2;
                mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, paramInt1, paramInt2, true);
                return;
            }
        }
    }
}