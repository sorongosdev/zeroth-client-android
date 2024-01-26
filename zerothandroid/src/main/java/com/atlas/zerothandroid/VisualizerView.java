package com.atlas.zerothandroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * A class that draws visualizations of data received from {@link ZerothAudioRecordRunnable}
 * Created by tyorikan on 2015/06/08.
 */
public class VisualizerView extends FrameLayout {
    /*
    녹음시 화면 중앙에 나오는 visualizer를 관리하는 클래스
    실제 녹음시에는 ZerothAudioRecordRunnable에서 음향의 크기 정보를 받아들이고 적용
     */

    /*
    나오는 막대의 높이나 비율을 수정하려면 getRandomHeight 함수를 수정
    Ui를 변경하는 함수를 수정하려면 recieve 함수를 수정
     */

    /*
        RENDAR_RANGE_TOP: 위로만 직사각형이 그려짐
        RENDAR_RANGE_BOTTOM: 아래로만 직사각형이 그려짐
        RENDAR_RANGE_TOP_BOTTOM: 위아래로 직사각형이 그려짐
     */

    private static final int DEFAULT_NUM_COLUMNS = 20;
    private static final int RENDAR_RANGE_TOP = 0;
    private static final int RENDAR_RANGE_BOTTOM = 1;
    private static final int RENDAR_RANGE_TOP_BOTTOM = 2;

    private int mNumColumns;
    private int mRenderColor;
    private int mType;
    private int mRenderRange;

    private int mBaseY;

    //view에 들어갈 객체들
    private Canvas mCanvas;
    private Bitmap mCanvasBitmap;
    private Rect mRect = new Rect();
    private Paint mPaint = new Paint();
    private Paint mFadePaint = new Paint();

    private float mColumnWidth;
    private float mSpace;

    public VisualizerView(Context context, AttributeSet attrs) {
        /*
        view의 constructor로 색,막대의 방향, 막대의 개수등을 결정하며 visualizerview 객체를 생성
         */
        super(context, attrs);
        init(context, attrs);
        mPaint.setColor(mRenderColor);
        mFadePaint.setColor(Color.argb(138, 255, 255, 255));
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray args = context.obtainStyledAttributes(attrs, R.styleable.visualizerView);
        mNumColumns = args.getInteger(R.styleable.visualizerView_numColumns, DEFAULT_NUM_COLUMNS);
        mRenderColor = args.getColor(R.styleable.visualizerView_renderColor, Color.WHITE);
        mType = args.getInt(R.styleable.visualizerView_renderType, Type.BAR.getFlag());
        mRenderRange = args.getInteger(R.styleable.visualizerView_renderRange, RENDAR_RANGE_TOP);
        args.recycle();
    }

    /**
     * @param baseY center Y position of visualizer
     */

    public void setBaseY(int baseY) {
        mBaseY = baseY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*
        Canvas를 생성하고 bitmap을 이용하여 막대를 생성하는 함수
         */
        super.onDraw(canvas);

        // Create canvas once we're ready to draw
        mRect.set(0, 0, getWidth(), getHeight());

        if (mCanvasBitmap == null) {
            mCanvasBitmap = Bitmap.createBitmap(
                    canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }

        if (mCanvas == null) {
            mCanvas = new Canvas(mCanvasBitmap);
        }

        if (mNumColumns > getWidth()) {
            mNumColumns = DEFAULT_NUM_COLUMNS;
        }

        mColumnWidth = (float) getWidth() / (float) mNumColumns;
        mSpace = mColumnWidth / 8f;

        if (mBaseY == 0) {
            mBaseY = getHeight() / 2;
        }

        canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
    }

    /**
     * receive volume from {@link ZerothAudioRecordRunnable}
     *
     * @param volume volume from mic input
     */
    public void receive(final int volume) {
        /*
        Handler를 통해 mainthread로 받은 음성의 크기를 설정된 타입의 막대의 길이로 표현
         */
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mCanvas == null) {
                    return;
                }

                if (volume == 0) {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                } else if ((mType & Type.FADE.getFlag()) != 0) {
                    // Fade out old contents
                    mFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
                    mCanvas.drawPaint(mFadePaint);
                } else {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }

                if ((mType & Type.BAR.getFlag()) != 0) {
                    drawBar(volume);
                }
                if ((mType & Type.PIXEL.getFlag()) != 0) {
                    drawPixel(volume);
                }
                invalidate();
            }
        });
    }

    private void drawBar(int volume) {
        /*
        하단의 getRandomHeight 함수를 이용하여 얻은 길이로 막대를 그림
         */
        for (int i = 0; i < mNumColumns; i++) {
            float height = getRandomHeight(volume);
            float left = i * mColumnWidth + mSpace;
            float right = (i + 1) * mColumnWidth - mSpace;

            RectF rect = createRectF(left, right, height);
            mCanvas.drawRect(rect, mPaint);
        }
    }

    private void drawPixel(int volume) {
        /*
        drawBar와 같은 기능을 하는 함수로 픽셀로 표현
         */
        for (int i = 0; i < mNumColumns; i++) {
            float height = getRandomHeight(volume);
            float left = i * mColumnWidth + mSpace;
            float right = (i + 1) * mColumnWidth - mSpace; // 여기까지 동일

            int drawCount = (int) (height / (right - left));
            if (drawCount == 0) {
                drawCount = 1;
            }
            float drawHeight = height / drawCount;

            // 각각의 픽셀을 그리는 부분
            for (int j = 0; j < drawCount; j++) {

                float top, bottom;
                RectF rect;

                switch (mRenderRange) {
                    case RENDAR_RANGE_TOP:
                        bottom = mBaseY - (drawHeight * j);
                        top = bottom - drawHeight + mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;

                    case RENDAR_RANGE_BOTTOM:
                        top = mBaseY + (drawHeight * j);
                        bottom = top + drawHeight - mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;

                    case RENDAR_RANGE_TOP_BOTTOM:
                        bottom = mBaseY - (height / 2) + (drawHeight * j);
                        top = bottom - drawHeight + mSpace;
                        rect = new RectF(left, top, right, bottom);
                        break;

                    default:
                        return;
                }
                mCanvas.drawRect(rect, mPaint);
            }
        }
    }

    private float getRandomHeight(int volume) {
        /*
        입력받은 음성의 크기로 visualizer의 막대의 길이를 반환하는 함수
         */
        double randomVolume = Math.random() * volume + 1;
        float height = getHeight();
        switch (mRenderRange) {
            case RENDAR_RANGE_TOP:
                height = mBaseY;
                break;
            case RENDAR_RANGE_BOTTOM:
                height = (getHeight() - mBaseY);
                break;
            case RENDAR_RANGE_TOP_BOTTOM:
                height = getHeight();
                break;
        }
        return (height / 60f) * (float) randomVolume;
    }

    private RectF createRectF(float left, float right, float height) {
        /*
        직사각형 클래스인 RectF 객체를 생성하여 반환
         */
        switch (mRenderRange) {
            case RENDAR_RANGE_TOP:
                return new RectF(left, mBaseY - height, right, mBaseY);
            case RENDAR_RANGE_BOTTOM:
                return new RectF(left, mBaseY, right, mBaseY + height);
            case RENDAR_RANGE_TOP_BOTTOM:
                return new RectF(left, mBaseY - height, right, mBaseY + height);
            default:
                return new RectF(left, mBaseY - height, right, mBaseY);
        }
    }

    /**
     * visualizer type
     */
    public enum Type {
        /*
        가능한 각각의 타입들을 enum으로 미리 지정
         */
        BAR(0x1), PIXEL(0x2), FADE(0x4);

        private int mFlag;

        Type(int flag) {
            mFlag = flag;
        }

        public int getFlag() {
            return mFlag;
        }
    }
}
