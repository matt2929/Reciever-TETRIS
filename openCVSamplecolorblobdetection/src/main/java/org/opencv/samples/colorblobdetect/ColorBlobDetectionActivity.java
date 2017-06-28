package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.SeekBar;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private boolean mIsColorSelected = false;
    private Mat mRgbaGr;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;

    private CameraBridgeViewBase mOpenCvCameraView;

    SeekBar seekWidth;
    SeekBar seekHeight;
    int widthBox = 1;
    int heightBox = 1;
    int buffer = 70;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.color_blob_detection_surface_view);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        seekHeight = (SeekBar) findViewById(R.id.seekBarHeight);
        seekWidth = (SeekBar) findViewById(R.id.seekBarWidth);
        SeekBar seekArea = (SeekBar) findViewById(R.id.minContourSeek);
        seekArea.setProgress(10);
        seekArea.setMax(1000);
        seekArea.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mDetector.setMinContourArea(((double)i/100.0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                heightBox = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                widthBox = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgbaGr = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(2255,255,0, 255);
    }

    public void onCameraViewStopped() {
        mRgbaGr.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgbaGr.cols();
        int rows = mRgbaGr.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x > 4) ? x - 4 : 0;
        touchedRect.y = (y > 4) ? y - 4 : 0;

        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgbaGr.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;


        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorHsv.val[0] + ", " + mBlobColorHsv.val[1] +
                ", " + mBlobColorHsv.val[2] + ", " + mBlobColorHsv.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgbaGr = inputFrame.rgba();
        int sqr1x, sqr1y, sqr2x, sqr2y;
        Scalar blueHSV, greenHSV, redHSV;
        float[] tempf = new float[3];
        double[] tempd = new double[3];
        greenHSV = new Scalar(85.234375, 254.765625, 181.890625, 0.0);
        redHSV = new Scalar(1.53125, 255.0, 195.640625, 0.0);
        blueHSV = new Scalar(171.0, 255.0, 172.875, 0.0);

        int widthOffset = widthBox;
        int heightOffset = heightBox;
        mRgbaGr.width();
        mRgbaGr.height();
        seekHeight.setMax(mRgbaGr.height() - (buffer + 5));
        seekWidth.setMax(mRgbaGr.width() - (buffer + 5));

        sqr1x = (mRgbaGr.width() / 2) - (widthOffset / 2);
        sqr2x = (mRgbaGr.width() / 2) + (widthOffset / 2);
        sqr1y = (mRgbaGr.height() / 2) - (heightOffset / 2);
        sqr2y = (mRgbaGr.height() / 2) + (heightOffset / 2);

        Point TopLeft = new Point(sqr1x, sqr1y);
        Point TopRight = new Point(sqr2x, sqr1y);
        Point BottomLeft = new Point(sqr1x, sqr2y);
        Point BottomRight = new Point(sqr2x, sqr2y);

        Point TopLeftM = new Point(sqr1x - buffer, sqr1y - buffer);
        Point TopRightM = new Point(sqr2x + buffer, sqr1y - buffer);
        Point BottomLeftM = new Point(sqr1x - buffer, sqr2y + buffer);
        Point BottomRightM = new Point(sqr2x + buffer, sqr2y + buffer);

        List<MatOfPoint> contours = new ArrayList<>();

        mDetector.setHsvColor(blueHSV);
        mDetector.process(mRgbaGr, TopLeftM, TopLeft);
        contours.addAll(mDetector.getContours());
        Point pTopLeft=mDetector.centerAverage();

        mDetector.setHsvColor(redHSV);
        mDetector.process(mRgbaGr, TopRightM, TopRight);
        contours.addAll(mDetector.getContours());
        Point pTopRight=mDetector.centerAverage();

        mDetector.setHsvColor(greenHSV);
        mDetector.process(mRgbaGr, BottomLeftM, BottomLeft);
        contours.addAll(mDetector.getContours());
        Point pBottomLeft=mDetector.centerAverage();


        mDetector.setHsvColor(blueHSV);
        mDetector.process(mRgbaGr, BottomRightM, BottomRight);
        contours.addAll(mDetector.getContours());
        Point pBottomRight=mDetector.centerAverage();


        Log.e(TAG, "Contours count: " + contours.size());
        Imgproc.drawContours(mRgbaGr, contours, -1, CONTOUR_COLOR,7);

        Imgproc.line(mRgbaGr, TopLeftM, BottomLeftM, new Scalar(0, 0, 0), 5);
        Imgproc.line(mRgbaGr, TopLeftM, TopRightM, new Scalar(0, 0, 0), 5);
        Imgproc.line(mRgbaGr, TopRightM, BottomRightM, new Scalar(0, 0, 0), 5);
        Imgproc.line(mRgbaGr, BottomLeftM, BottomRightM, new Scalar(0, 0, 0), 5);

        Imgproc.rectangle(mRgbaGr, TopLeftM, TopLeft, new Scalar(12, 28, 181), 5);
        Imgproc.rectangle(mRgbaGr, TopRightM, TopRight, new Scalar(162, 0, 0), 5);
        Imgproc.rectangle(mRgbaGr, BottomLeft, BottomLeftM, new Scalar(26, 173, 18), 5);
        Imgproc.rectangle(mRgbaGr, BottomRight, BottomRightM, new Scalar(12, 28, 181), 5);

        Imgproc.circle(mRgbaGr, pTopLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pTopRight, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomRight, 10, new Scalar(255, 21, 255), 5);

        Imgproc.line(mRgbaGr, pTopLeft, pTopRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopLeft, pBottomLeft,new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pBottomLeft, pBottomRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopRight, pBottomRight,new Scalar(117, 210, 173), 5);

        return mRgbaGr;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        return new Scalar(pointMatRgba.get(0, 0));
    }
}
