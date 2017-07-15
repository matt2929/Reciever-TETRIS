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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
    TargetView topLeftTarget;
    TargetView topRightTarget;
    TargetView bottomRightTarget;
    TargetView bottomLeftTarget;
    private Size SPECTRUM_SIZE;
    private CameraBridgeViewBase mOpenCvCameraView;

    float dXTL, dYTL, dXTR, dYTR, dXBL, dYBL, dXBR, dYBR;
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
                saveThisCapture = true;
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

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //this is the frame we want it in RGBA
        mRgbaGr = inputFrame.rgba()  ;
        Scalar blueHSV, greenHSV, redHSV; // our hard coded colors to detect
        width = mRgbaGr.width(); // what is the width and height of frame
        height = mRgbaGr.height();
        greenHSV = new Scalar(89.890625, 201.28125, 235.703125, 0.0);//dat harcoding TODO:check validity of these values
        redHSV = new Scalar(2.734375, 255.0, 220.203125, 0.0);
        blueHSV = new Scalar(171.0, 255.0, 205.28125, 0.0);

        //Grab bottom right point of target view and convert it as the resolution of the camera picture and the phone screen could be different
        Point TopLeft = conversion(new Point(topLeftTarget.getX() + topLeftTarget.getWidth(), topLeftTarget.getY() + topLeftTarget.getHeight()));
        Point TopRight = conversion(new Point(topRightTarget.getX() + topRightTarget.getWidth(), topRightTarget.getY() + topRightTarget.getHeight()));
        Point BottomLeft = conversion(new Point(bottomLeftTarget.getX() + bottomLeftTarget.getWidth(), bottomLeftTarget.getY() + bottomLeftTarget.getHeight()));
        Point BottomRight = conversion(new Point(bottomRightTarget.getX() + bottomRightTarget.getWidth(), bottomRightTarget.getY() + bottomRightTarget.getHeight()));

        //Grab top left do same coversion
        Point TopLeftM = conversion(new Point(topLeftTarget.getX(), topLeftTarget.getY()));
        Point TopRightM = conversion(new Point(topRightTarget.getX(), topRightTarget.getY()));
        Point BottomLeftM = conversion(new Point(bottomLeftTarget.getX(), bottomLeftTarget.getY()));
        Point BottomRightM = conversion(new Point(bottomRightTarget.getX(), bottomRightTarget.getY()));

        //run the algorithm to find the center of the black region in the corner
        Point pTopLeft = mDetector.getCenterBlack(mRgbaGr, TopLeft, TopLeftM, blueHSV,0);

        Point pTopRight = mDetector.getCenterBlack(mRgbaGr, TopRight, TopRightM, redHSV,1);
        Point pBottomLeft = mDetector.getCenterBlack(mRgbaGr, BottomLeft, BottomLeftM, greenHSV,2);
        Point pBottomRight = mDetector.getCenterBlack(mRgbaGr, BottomRight, BottomRightM, blueHSV,3);

        //this is where we will save our timing block points
        List<Point> topLine = new ArrayList<Point>();
        List<Point> bottomLine = new ArrayList<Point>();
        List<Point> leftLine = new ArrayList<Point>();
        List<Point> rightLine = new ArrayList<Point>();

        if (pTopLeft.x != -100 && pTopRight.x != -100 && pBottomLeft.x != -100 && pBottomRight.x != -100) {//AKA only if we found four corner points do we continue
            //we automaticaly readjust where the target views are centered but only if we dont move them of screen cuz that crashes app :''( ; therefore, safe move
           safeMove(topLeftTarget, conversionX(pTopLeft.x) - (topLeftTarget.getWidth() / 2), (conversionY(pTopLeft.y)) - (topLeftTarget.getWidth() / 2));
            safeMove(topRightTarget, conversionX(pTopRight.x) - (topLeftTarget.getWidth() / 2), conversionY(pTopRight.y) - (topLeftTarget.getWidth() / 2));
            safeMove(bottomLeftTarget, conversionX(pBottomLeft.x) - (topLeftTarget.getWidth() / 2), conversionY(pBottomLeft.y) - (topLeftTarget.getWidth() / 2));
            safeMove(bottomRightTarget, conversionX(pBottomRight.x) - (topLeftTarget.getWidth() / 2), conversionY(pBottomRight.y) - (topLeftTarget.getWidth() / 2));
            //find those timing son
            bottomLine = mDetector.findTimingHorizontal(mRgbaGr, pTopLeft, pTopRight);
            topLine = mDetector.findTimingHorizontal(mRgbaGr, pBottomLeft, pBottomRight);
            leftLine = mDetector.findTimingVerticle(mRgbaGr, pTopLeft, pBottomLeft);
            rightLine = mDetector.findTimingVerticle(mRgbaGr, pTopRight, pBottomRight);
        }
        if (bottomLine.size() == topLine.size() && leftLine.size() == rightLine.size()) {//if the amount of timing blocks located are consistent per orientation continue else cry
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
        }/*
        mRgbaGr=inputFrame.rgba();*/
        Imgproc.rectangle(mRgbaGr, TopLeftM, TopLeft, new Scalar(12, 28, 181), 5);
        Imgproc.rectangle(mRgbaGr, TopRightM, TopRight, new Scalar(162, 0, 0), 5);
        Imgproc.rectangle(mRgbaGr, BottomLeft, BottomLeftM, new Scalar(26, 173, 18), 5);
        Imgproc.rectangle(mRgbaGr, BottomRight, BottomRightM, new Scalar(12, 28, 181), 5);

        //draw pink points for center of corner
        Imgproc.circle(mRgbaGr, pTopLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pTopRight, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomRight, 10, new Scalar(255, 21, 255), 5);

        //draw lines that connect assumed corners
        Imgproc.line(mRgbaGr, pTopLeft, pTopRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopLeft, pBottomLeft, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pBottomLeft, pBottomRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopRight, pBottomRight, new Scalar(117, 210, 173), 5);

        //draw all data center color points
        ArrayList<String> valueCalc = new ArrayList<>();
        for (int p = 0; p < innerGrid.size(); p++) {

            Imgproc.circle(mRgbaGr, innerGrid.get(p), 10, new Scalar(76, 123, 254, 255), 5);

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
                        minValue=findMin[i];
                    }
                }
                switch (minIndex) {
                    case 0:
                        valueCalc.add("White");
                        break;
                    case 1:
                        valueCalc.add("Blue");
                        break;
                    case 2:
                        valueCalc.add("Red");
                        break;
                    case 3:
                        valueCalc.add("Green");
                        break;
                }
            }
        }


        //draw timing points
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
        //end timing points
        if (saveThisCapture) {
            Integer average1 = averageBlockSize(topLine),
                    average2 = averageBlockSize(leftLine),
                    average3 = averageBlockSize(rightLine),
                    average4 = averageBlockSize(bottomLine);
            double average = (average1 + average2 + average3 + average4) / 4.0;
            SaveValues saveValues = new SaveValues((int) average, topLine.size(), leftLine.size());
            saveValues.saveBarCode(getApplicationContext(), valueCalc);
            saveThisCapture = false;
        }
        return mRgbaGr;
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
        int averageX = (maxX - minX) / points.size();
        int averageY = (maxY - minY) / points.size();
        return ((averageX + averageY) / 2);
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
}