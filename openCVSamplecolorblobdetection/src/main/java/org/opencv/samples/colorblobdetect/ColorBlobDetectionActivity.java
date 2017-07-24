package org.opencv.samples.colorblobdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.widget.Button;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";
    private boolean transmissionStarted = false;
    private boolean mIsColorSelected = false;
    private Mat mRgbaGr;
    ArrayList<Point> innerGrid = new ArrayList<Point>();
    private Button saveDatum;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private boolean saveThisCapture = false;
    //use view to hand select corners
    WhatDoISee whatDoISee, whatDoISee2;
    TargetView topLeftTarget;
    TargetView topRightTarget;
    TargetView bottomRightTarget;
    TargetView bottomLeftTarget;
    Scalar greenHSV = new Scalar(89.890625, 201.28125, 235.703125, 0.0);//dat harcoding TODO:check validity of these values
    Scalar redHSV = new Scalar(2.734375, 255.0, 220.203125, 0.0);
    Scalar blueHSV = new Scalar(171.0, 255.0, 205.28125, 0.0);

    private Size SPECTRUM_SIZE;
    private double[] synchBlockColorLast;
    Long UpdateRate = Long.valueOf(50);
    Point pTopLeft;
    Point pTopRight;
    Point pBottomLeft;
    Point pBottomRight;
    Point TopLeft;
    Point TopRight;
    Point BottomLeft;
    Point BottomRight;
    Point TopLeftM;
    Point TopRightM;
    Point BottomLeftM;
    Point BottomRightM;

    private Boolean startedTrans = false;
    private Boolean endedTrans = false;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Long LastTime = System.currentTimeMillis();
    float dXTL, dYTL, dXTR, dYTR, dXBL, dYBL, dXBR, dYBR;
    int countScreen = -1;
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
    private int width, height;
    private List<Point> bottomLine, topLine, leftLine, rightLine;
    private boolean hasHappenedOnce = false;
    private boolean canAnalyze = false;

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
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //Tell target view what to do when you click on it -------------------------------------------------------------------------------------------------
        topLeftTarget = (TargetView) findViewById(R.id.topLeft);
        topLeftTarget.setOnTouchListener(new OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXTL = view.getX() - event.getRawX();
                        dYTL = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (inBounds((int) event.getRawX() + (int) dXTL, (int) event.getRawY() + (int) dYTL, view.getWidth(), view.getHeight())) {
                            view.animate()
                                    .x(event.getRawX() + dXTL)
                                    .y(event.getRawY() + dYTL)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        topRightTarget = (TargetView) findViewById(R.id.topRight);
        topRightTarget.setOnTouchListener(new OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXTR = view.getX() - event.getRawX();
                        dYTR = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (inBounds((int) event.getRawX() + (int) dXTR, (int) event.getRawY() + (int) dYTR, view.getWidth(), view.getHeight())) {

                            view.animate()
                                    .x(event.getRawX() + dXTR)
                                    .y(event.getRawY() + dYTR)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        bottomRightTarget = (TargetView) findViewById(R.id.bottomRight);
        bottomRightTarget.setOnTouchListener(new OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        dXBR = view.getX() - event.getRawX();
                        dYBR = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (inBounds((int) event.getRawX() + (int) dXBR, (int) event.getRawY() + (int) dYBR, view.getWidth(), view.getHeight())) {

                            view.animate()
                                    .x(event.getRawX() + dXBR)
                                    .y(event.getRawY() + dYBR)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        bottomLeftTarget = (TargetView) findViewById(R.id.bottomLeft);
        bottomLeftTarget.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dXBL = view.getX() - event.getRawX();
                        dYBL = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (inBounds((int) event.getRawX() + (int) dXBL, (int) event.getRawY() + (int) dYBL, view.getWidth(), view.getHeight())) {

                            view.animate()
                                    .x(event.getRawX() + dXBL)
                                    .y(event.getRawY() + dYBL)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        //Tell target view what to do when you click on it -------------------------------------------------------------------------------------------------

        //Set color and text value of target view
        topLeftTarget.SetUp(Color.BLUE, "TL");
        topRightTarget.SetUp(Color.RED, "TR");
        bottomLeftTarget.SetUp(Color.GREEN, "BL");
        bottomRightTarget.SetUp(Color.BLUE, "BR");

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        saveDatum = (Button) findViewById(R.id.saveThatData);
        saveDatum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transmissionStarted = true;
                saveDatum.setVisibility(View.GONE);
                UpdateRate = Long.valueOf(5000);
            }
        });
        whatDoISee = (WhatDoISee) findViewById(R.id.what);
        whatDoISee2 = (WhatDoISee) findViewById(R.id.what2);
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
    }

    public void onCameraViewStopped() {
        mRgbaGr.release();
    }


    public boolean onTouch(View v, MotionEvent event) {
        //this currently does nothing
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

    public boolean getTheTimingBlocks() {
        List<Point> pointsTemp1 = mDetector.findTimingHorizontal(mRgbaGr, pTopLeft, pTopRight);
        List<Point> pointsTemp2 = mDetector.findTimingHorizontal(mRgbaGr, pBottomLeft, pBottomRight);
        if (pointsTemp1.size() == pointsTemp2.size()) {
            bottomLine = mDetector.findTimingHorizontal(mRgbaGr, pTopLeft, pTopRight);
            topLine = mDetector.findTimingHorizontal(mRgbaGr, pBottomLeft, pBottomRight);
        } else {
            return false;
        }
        pointsTemp1 = mDetector.findTimingVerticle(mRgbaGr, pTopLeft, pBottomLeft);
        pointsTemp2 = mDetector.findTimingVerticle(mRgbaGr, pTopRight, pBottomRight);
        if (pointsTemp1.size() == pointsTemp2.size()) {
            leftLine = mDetector.findTimingVerticle(mRgbaGr, pTopLeft, pBottomLeft);
            rightLine = mDetector.findTimingVerticle(mRgbaGr, pTopRight, pBottomRight);
        } else {
            return false;
        }
        if (bottomLine.size() != 0 && leftLine.size() != 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    saveDatum.setBackgroundColor(Color.WHITE);
                }
            });
            return true;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                saveDatum.setBackgroundColor(Color.YELLOW);
            }
        });
        return false;
    }

    public void getInnerGrid() {
        innerGrid.clear();
        for (int y = 0; y < leftLine.size(); y++) {
            for (int x = 0; x < topLine.size(); x++) {
                //watch your step line intersect equation below
                //this is where we draw every point that lies on the matrix of colors

                double slope1 = (leftLine.get(y).y - rightLine.get(y).y) / (leftLine.get(y).x - rightLine.get(y).x);
                double yIntercept1 = leftLine.get(y).y - (slope1 * leftLine.get(y).x);
                double slope2 = (topLine.get(x).y - bottomLine.get(x).y) / (topLine.get(x).x - bottomLine.get(x).x);
                double yIntercept2 = topLine.get(x).y - (slope2 * topLine.get(x).x);
                int xIntercept = (int) ((yIntercept2 - yIntercept1) / (slope1 - slope2));
                int yIntercept = (int) (xIntercept * slope1) + (int) yIntercept1;
                innerGrid.add(new Point(xIntercept, yIntercept));

            }
        }
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Long timeShortWise =System.currentTimeMillis();
        Long timeLongWise =System.currentTimeMillis();
        mRgbaGr = inputFrame.rgba();
        if (Math.abs(System.currentTimeMillis() - LastTime) > UpdateRate || pTopLeft == null) {
            timeShortWise =System.currentTimeMillis();
            targetUpdate();
            Log.e("target update",""+Math.abs(timeShortWise-System.currentTimeMillis()));
            timeShortWise =System.currentTimeMillis();
            if (findCenterPoints()) {
                safelyMoveAll();
                Log.e("safely move all",""+Math.abs(timeShortWise-System.currentTimeMillis()));
                timeShortWise =System.currentTimeMillis();
                if (getTheTimingBlocks()) {
                    Log.e("timing block",""+Math.abs(timeShortWise-System.currentTimeMillis()));
                    timeShortWise =System.currentTimeMillis();
                    hasHappenedOnce = true;
                } else {
                }
            } else {
            }
            LastTime = System.currentTimeMillis();
        }
        Log.e("synch","Start\n");

        if (leftLine != null && bottomLine != null) {
            double[] currentStateBlock = mDetector.getStateBlock(mRgbaGr);
            double[] currentStateBlock2 = mDetector.getStateBlock2(mRgbaGr);
            colorSynchDetermination(currentStateBlock, currentStateBlock2);
            Log.e("synch",""+Math.abs(timeShortWise-System.currentTimeMillis()));
            timeShortWise =System.currentTimeMillis();
            getInnerGrid();
            Log.e("inner grid",""+Math.abs(timeShortWise-System.currentTimeMillis()));
            timeShortWise =System.currentTimeMillis();
            saveRoutine(determineColors());
            Log.e("save & determine colors",""+Math.abs(timeShortWise-System.currentTimeMillis()));
            timeShortWise =System.currentTimeMillis();
            drawTimingAndLine();
            Log.e("draw timing and line",""+Math.abs(timeShortWise-System.currentTimeMillis()));
            timeShortWise =System.currentTimeMillis();
        }
        drawCornerData();
        Log.e("corner draw",""+Math.abs(timeShortWise-System.currentTimeMillis()));
        timeShortWise =System.currentTimeMillis();
        Log.e("synch-end","\nEND\nTotal:"+Math.abs(timeLongWise -System.currentTimeMillis()));
        return mRgbaGr;
    }


    public ArrayList<String> determineColors() {
        ArrayList<String> valueCalc = new ArrayList<>();
        int colorChoice = 0;
        for (int p = 0; p < innerGrid.size(); p++) {
            if (saveThisCapture) {
                int[] colorLikiness = new int[]{0, 0, 0};
                double[] color = mRgbaGr.get((int) innerGrid.get(p).y, (int) innerGrid.get(p).x);
                double whiteDiff, blueDiff, greenDiff, redDiff;
                double[] greenRGB = new double[]{0, 255, 0, 0.0};
                double[] redRGB = new double[]{255, 0, 0, 0.0};
                double[] blueRGB = new double[]{0, 0, 255, 0.0};
                double[] whiteRGB = new double[]{255, 255, 255, 0.0};

                whiteDiff = colorDifference(whiteRGB, color);
                blueDiff = colorDifference(blueRGB, color);
                greenDiff = colorDifference(greenRGB, color);
                redDiff = colorDifference(redRGB, color);

                double[] findMin = new double[]{whiteDiff, blueDiff, redDiff, greenDiff};
                int minIndex = -1;
                double minValue = Double.MAX_VALUE;
                for (int i = 0; i < findMin.length; i++) {
                    if (findMin[i] < minValue) {
                        minIndex = i;
                        minValue = findMin[i];
                    }
                }
                switch (minIndex) {
                    case 0:
                        valueCalc.add("W");
                        break;
                    case 1:
                        valueCalc.add("B");
                        break;
                    case 2:
                        valueCalc.add("R");
                        break;
                    case 3:
                        valueCalc.add("G");
                        break;
                }
            }
            if (colorChoice != 255) {
                colorChoice++;
            }
            Imgproc.circle(mRgbaGr, innerGrid.get(p), 1, new Scalar(colorChoice, colorChoice, colorChoice, 255), 1);

        }
        return valueCalc;
    }

    public void drawTimingAndLine() {
        for (Point p : topLine) {
            Imgproc.circle(mRgbaGr, p, 10, new Scalar(23, 255, 143, 255), 5);

        }
        for (Point p : bottomLine) {
            Imgproc.circle(mRgbaGr, p, 10, new Scalar(23, 255, 143, 255), 5);
        }
        for (Point p : leftLine) {
            Imgproc.circle(mRgbaGr, p, 10, new Scalar(23, 255, 143, 255), 5);
        }
        for (Point p : rightLine) {
            Imgproc.circle(mRgbaGr, p, 10, new Scalar(23, 255, 143, 255), 5);
        }
        //draw lines that connect assumed corners
        Imgproc.line(mRgbaGr, pTopLeft, pTopRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopLeft, pBottomLeft, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pBottomLeft, pBottomRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopRight, pBottomRight, new Scalar(117, 210, 173), 5);

    }

    public void saveRoutine(final ArrayList<String> valueCalc) {
        new Thread(new Runnable() {
            public void run() {
                if (valueCalc != null) {
                    if (saveThisCapture) {
                        Integer average1 = averageBlockSize(topLine),
                                average2 = averageBlockSize(leftLine),
                                average3 = averageBlockSize(rightLine),
                                average4 = averageBlockSize(bottomLine);
                        double average = (average1 + average2 + average3 + average4) / 4.0;
                        countScreen++;
                        SaveValues saveValues = new SaveValues((int) average, topLine.size(), leftLine.size(), "" + countScreen);
                        saveValues.saveBarCode(getApplicationContext(), valueCalc);
                        saveThisCapture = false;
                    }
                }
            }
        }).start();
    }

    public void drawCornerData() {
        Imgproc.circle(mRgbaGr, pTopLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pTopRight, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomRight, 10, new Scalar(255, 21, 255), 5);
        Imgproc.rectangle(mRgbaGr, TopLeftM, TopLeft, new Scalar(12, 28, 181), 5);
        Imgproc.rectangle(mRgbaGr, TopRightM, TopRight, new Scalar(162, 0, 0), 5);
        Imgproc.rectangle(mRgbaGr, BottomLeft, BottomLeftM, new Scalar(26, 173, 18), 5);
        Imgproc.rectangle(mRgbaGr, BottomRight, BottomRightM, new Scalar(12, 28, 181), 5);
    }


    public void targetUpdate() {
        //Grab bottom right point of target view and convert it as the resolution of the camera picture and the phone screen could be different
        width = mRgbaGr.width(); // what is the width and height of frame
        height = mRgbaGr.height();
        TopLeft = conversion(new Point(topLeftTarget.getX() + topLeftTarget.getWidth(), topLeftTarget.getY() + topLeftTarget.getHeight()));
        TopRight = conversion(new Point(topRightTarget.getX() + topRightTarget.getWidth(), topRightTarget.getY() + topRightTarget.getHeight()));
        BottomLeft = conversion(new Point(bottomLeftTarget.getX() + bottomLeftTarget.getWidth(), bottomLeftTarget.getY() + bottomLeftTarget.getHeight()));
        BottomRight = conversion(new Point(bottomRightTarget.getX() + bottomRightTarget.getWidth(), bottomRightTarget.getY() + bottomRightTarget.getHeight()));

        //Grab top left do same coversion
        TopLeftM = conversion(new Point(topLeftTarget.getX(), topLeftTarget.getY()));
        TopRightM = conversion(new Point(topRightTarget.getX(), topRightTarget.getY()));
        BottomLeftM = conversion(new Point(bottomLeftTarget.getX(), bottomLeftTarget.getY()));
        BottomRightM = conversion(new Point(bottomRightTarget.getX(), bottomRightTarget.getY()));

    }

    public boolean findCenterPoints() {
        pTopLeft = mDetector.getCenterBlack(mRgbaGr, TopLeft, TopLeftM, blueHSV, 0);
        pTopRight = mDetector.getCenterBlack(mRgbaGr, TopRight, TopRightM, redHSV, 1);
        pBottomLeft = mDetector.getCenterBlack(mRgbaGr, BottomLeft, BottomLeftM, greenHSV, 2);
        pBottomRight = mDetector.getCenterBlack(mRgbaGr, BottomRight, BottomRightM, blueHSV, 3);
        return (pTopLeft.x != -100 && pTopRight.x != -100 && pBottomLeft.x != -100 && pBottomRight.x != -100);
    }


    public void safelyMoveAll() {
        safeMove(topLeftTarget, conversionX(pTopLeft.x) - (topLeftTarget.getWidth() / 2), (conversionY(pTopLeft.y)) - (topLeftTarget.getWidth() / 2));
        safeMove(topRightTarget, conversionX(pTopRight.x) - (topLeftTarget.getWidth() / 2), conversionY(pTopRight.y) - (topLeftTarget.getWidth() / 2));
        safeMove(bottomLeftTarget, conversionX(pBottomLeft.x) - (topLeftTarget.getWidth() / 2), conversionY(pBottomLeft.y) - (topLeftTarget.getWidth() / 2));
        safeMove(bottomRightTarget, conversionX(pBottomRight.x) - (topLeftTarget.getWidth() / 2), conversionY(pBottomRight.y) - (topLeftTarget.getWidth() / 2));
    }

    public void getColorContour(Mat mRgbaGr, Point point1, Point point2, Scalar color) {

    }

    public Integer averageBlockSize(List<Point> points) {
        Integer maxX = -1, minX = 1000, maxY = -1, minY = 1000;
        for (Point p : points) {
            if (p.x > maxX)
                maxX = (int) p.x;
            if (p.x < minX)
                minX = (int) p.x;
            if (p.y < minY)
                minY = (int) p.y;
            if (p.y > maxY)
                maxY = (int) p.y;
        }
        int averageX = -1;
        int averageY = -1;

        if (points.size() != 0) {
            averageX = (maxX - minX) / points.size();
            averageY = (maxY - minY) / points.size();
        } else {

        }
        return ((averageX + averageY) / 2);

    }

    public boolean isYellow(double[] color) {
        if (color == null) {
            return false;
        }
        if (color[2] < 60) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPink(double[] color) {
        if (color == null) {
            return false;
        }
        if (color[1] < 60) {
            return true;
        } else {
            return false;
        }
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
        return new Scalar(pointMatRgba.get(0, 0));

    }

    public boolean inBounds(int x, int y, int width, int height) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (x > 0 && y > 0) {
            if (x + width < metrics.widthPixels && y + height < metrics.heightPixels) {
                return true;
            }
        }
        return false;
    }

    public double colorDifference(double[] col1, double[] col2) {
        double sum = 0.0;
        for (int i = 0; i < col1.length; i++) {
            sum += Math.pow(col1[i] - col2[i], 2.0);
        }
        return Math.sqrt(sum);
    }


    public void safeMove(View view, float Xset, float Yset) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (Xset < 0
                || Yset < 0
                || Xset + view.getWidth() >= metrics.widthPixels
                || Yset + view.getHeight() >= metrics.heightPixels) {

        } else {
            view.setX(Xset);
            view.setY(Yset);
        }
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    public Point conversion(Point p) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int real_x = (int) (p.x * width) / metrics.widthPixels;
        int real_y = (int) (p.y * height) / metrics.heightPixels;
        return (new Point(real_x, real_y));
    }

    public float conversionX(double p) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (float) (p * metrics.widthPixels) / width;
    }


    public float conversionY(double p) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (float) (p * metrics.heightPixels) / height;
    }

    public void colorSynchDetermination(double[] color, final double[] color2) {
        boolean checkSame = true;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(color[i] - color2[i]) > 40) {
                checkSame = false;
            }
        }

        final double[] colorT = color;
        if (color != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    whatDoISee.updatePreview(colorT);
                    whatDoISee2.updatePreview(color2);
                }

            });
            if (checkSame) {
                if (!endedTrans && transmissionStarted) {
                    if (!startedTrans) {
                        if (isPink(color)) {
                            Log.e("state", "start");
                            startedTrans = true;
                            saveThisCapture = true;
                        }
                    } else {
                        if (isPink(color) && !isPink(synchBlockColorLast)) {
                            Log.e("state", "end");
                            endedTrans = true;
                            saveThisCapture = true;
                        } else if (!isPink(color) && isPink(synchBlockColorLast)) {
                            saveThisCapture = true;
                            Log.e("state", "first non pink");
                        } else if (isYellow(synchBlockColorLast) != isYellow(color)) {
                            saveThisCapture = true;
                            Log.e("state", "not equal");
                        } else if (isYellow(color) == isYellow(synchBlockColorLast)) {
                        }
                    }
                } else {
                }
                synchBlockColorLast = color;
            }
        }
    }
}

