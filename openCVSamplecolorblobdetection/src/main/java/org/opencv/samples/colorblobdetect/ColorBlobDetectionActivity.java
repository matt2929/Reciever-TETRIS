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
import org.opencv.core.MatOfPoint2f;
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

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private boolean mIsColorSelected = false;
    private Mat mRgbaGr;
    ArrayList<Point> innerGrid = new ArrayList<Point>();

    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    TargetView topLeftTarget;
    TargetView topRightTarget;
    TargetView bottomRightTarget;
    TargetView bottomLeftTarget;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private TargetView targetView;

    private CameraBridgeViewBase mOpenCvCameraView;

    int widthBox = 1;
    int heightBox = 1;
    int buffer = 70;
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
        topLeftTarget.SetUp(Color.BLUE, "TL");
        topRightTarget.SetUp(Color.RED, "TR");
        bottomLeftTarget.SetUp(Color.GREEN, "BL");
        bottomRightTarget.SetUp(Color.BLUE, "BR");
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
        CONTOUR_COLOR = new Scalar(2255, 255, 0, 255);
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
        Scalar blueHSV, greenHSV, redHSV;
        width = mRgbaGr.width();
        height = mRgbaGr.height();
        greenHSV = new Scalar(85.234375, 254.765625, 181.890625, 0.0);
        redHSV = new Scalar(1.53125, 255.0, 195.640625, 0.0);
        blueHSV = new Scalar(171.0, 255.0, 172.875, 0.0);

        mRgbaGr.width();
        mRgbaGr.height();

        Point TopLeft = conversion(new Point(topLeftTarget.getX() + topLeftTarget.getWidth(), topLeftTarget.getY() + topLeftTarget.getHeight()));
        Point TopRight = conversion(new Point(topRightTarget.getX() + topRightTarget.getWidth(), topRightTarget.getY() + topRightTarget.getHeight()));
        Point BottomLeft = conversion(new Point(bottomLeftTarget.getX() + bottomLeftTarget.getWidth(), bottomLeftTarget.getY() + bottomLeftTarget.getHeight()));
        Point BottomRight = conversion(new Point(bottomRightTarget.getX() + bottomRightTarget.getWidth(), bottomRightTarget.getY() + bottomRightTarget.getHeight()));

        Point TopLeftM = conversion(new Point(topLeftTarget.getX(), topLeftTarget.getY()));
        Point TopRightM = conversion(new Point(topRightTarget.getX(), topRightTarget.getY()));
        Point BottomLeftM = conversion(new Point(bottomLeftTarget.getX(), bottomLeftTarget.getY()));
        Point BottomRightM = conversion(new Point(bottomRightTarget.getX(), bottomRightTarget.getY()));

        Point pTopLeft = getCenterBlack(mRgbaGr, TopLeft, TopLeftM, blueHSV);
        Point pTopRight = getCenterBlack(mRgbaGr, TopRight, TopRightM, redHSV);
        Point pBottomLeft = getCenterBlack(mRgbaGr, BottomLeft, BottomLeftM, greenHSV);
        Point pBottomRight = getCenterBlack(mRgbaGr, BottomRight, BottomRightM, blueHSV);

        List<Point> topLine = new ArrayList<Point>();
        List<Point> bottomLine = new ArrayList<Point>();
        List<Point> leftLine = new ArrayList<Point>();
        List<Point> rightLine = new ArrayList<Point>();

        if (pTopLeft.x != -100 && pTopRight.x != -100 && pBottomLeft.x != -100 && pBottomRight.x != -100) {
                safeMove(topLeftTarget,conversionX(pTopLeft.x) - (topLeftTarget.getWidth() / 2),(conversionY(pTopLeft.y)) - (topLeftTarget.getWidth() / 2));
                safeMove(topRightTarget,conversionX(pTopRight.x) - (topLeftTarget.getWidth() / 2),conversionY(pTopRight.y) - (topLeftTarget.getWidth() / 2));
                safeMove(bottomLeftTarget,conversionX(pBottomLeft.x) - (topLeftTarget.getWidth() / 2),conversionY(pBottomLeft.y) - (topLeftTarget.getWidth() / 2));
                safeMove(bottomRightTarget,conversionX(pBottomRight.x) - (topLeftTarget.getWidth() / 2),conversionY(pBottomRight.y) - (topLeftTarget.getWidth() / 2));

            bottomLine = findTimingHorizontal(mRgbaGr, pTopLeft, pTopRight);
            topLine = findTimingHorizontal(mRgbaGr, pBottomLeft, pBottomRight);
            leftLine = findTimingVerticle(mRgbaGr, pTopLeft, pBottomLeft);
            rightLine = findTimingVerticle(mRgbaGr, pTopRight, pBottomRight);
        }
        if(bottomLine.size()==topLine.size()&&leftLine.size()==rightLine.size()){
            innerGrid.clear();
            for(int y=0;y<leftLine.size();y++){
                for(int x=0;x<topLine.size();x++){
                    double slope1=(leftLine.get(y).y-rightLine.get(y).y)/(leftLine.get(y).x-rightLine.get(y).x);
                    double yIntercept1 = leftLine.get(y).y-(slope1*leftLine.get(y).x);

                    double slope2=(topLine.get(x).y-bottomLine.get(x).y)/(topLine.get(x).x-bottomLine.get(x).x);
                    double yIntercept2 = topLine.get(x).y-(slope2*topLine.get(x).x);
                    int xIntercept = (int)((yIntercept2-yIntercept1)/(slope1-slope2));
                    int yIntercept = (int) (xIntercept*slope1)+(int)yIntercept1;
                    innerGrid.add(new Point(xIntercept,yIntercept));
                }
            }
        }

        Imgproc.rectangle(mRgbaGr, TopLeftM, TopLeft, new Scalar(12, 28, 181), 5);
        Imgproc.rectangle(mRgbaGr, TopRightM, TopRight, new Scalar(162, 0, 0), 5);
        Imgproc.rectangle(mRgbaGr, BottomLeft, BottomLeftM, new Scalar(26, 173, 18), 5);
        Imgproc.rectangle(mRgbaGr, BottomRight, BottomRightM, new Scalar(12, 28, 181), 5);

        Imgproc.circle(mRgbaGr, pTopLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pTopRight, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomLeft, 10, new Scalar(255, 21, 255), 5);
        Imgproc.circle(mRgbaGr, pBottomRight, 10, new Scalar(255, 21, 255), 5);

        Imgproc.line(mRgbaGr, pTopLeft, pTopRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopLeft, pBottomLeft, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pBottomLeft, pBottomRight, new Scalar(117, 210, 173), 5);
        Imgproc.line(mRgbaGr, pTopRight, pBottomRight, new Scalar(117, 210, 173), 5);

        for(Point p : innerGrid){
            Imgproc.circle(mRgbaGr, p, 10, new Scalar(76, 123, 254, 255), 5);
        }

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
        return mRgbaGr;
    }

    public void getColorContour(Mat mRgbaGr, Point point1, Point point2, Scalar color){

    }

    public Point getCenterBlack(Mat mRgbaGr, Point point1, Point point2, Scalar color) {
        List<MatOfPoint> contours = new ArrayList<>();
        mDetector.setHsvColor(color);
        MatOfPoint matOfPointMax = null;
        MatOfPoint matOfPointMax2 = null;

        if (mDetector.process(mRgbaGr, point1, point2)) {
            List<MatOfPoint> matOfPointList = mDetector.getContours();
            double areaMax = -1.0;
            double areaMax2 = -1.0;
            for (MatOfPoint matOfPoint : matOfPointList) {
                double areaT = Imgproc.contourArea(matOfPoint);
                if (areaT > areaMax) {
                    matOfPointMax = matOfPoint;
                    areaMax = areaT;
                } else {
                    if (areaT > areaMax2) {
                        matOfPointMax = matOfPoint;
                        areaMax2 = areaT;
                    }
                }
            }
        }
        contours.clear();
        if (matOfPointMax2 != null) {
            contours.add(matOfPointMax2);

        } else {
            contours.add(matOfPointMax);
        }
        if (matOfPointMax != null) {
            mDetector.setHsvBlack(color);
            MatOfPoint2f NewMtx = new MatOfPoint2f(matOfPointMax.toArray());
            Rect rect = Imgproc.minAreaRect(NewMtx).boundingRect();
            mDetector.process(mRgbaGr, rect.tl(), rect.br());
        }
        return mDetector.centerAverage();
    }

    public List<Point> findTimingHorizontal(Mat mat, Point start, Point end) {
        List<Point> points = new ArrayList<>();
        double rise = start.y - end.y;
        double run = start.x - end.x;
        double slope = rise / run;
        double yIntercept = start.y - (start.x * slope);
        int startBlack = -1;
        Log.e("Trace:", "From (" + start.x + ", " + start.y + ") " +
                "\nTo (" + end.x + ", " + end.y + ")" +
                "\nRise:" + rise + "" +
                "\nRun: " + run + "" +
                "\nSlope" + slope +
                "\nY-intercept" + yIntercept);
        int getOutBlack = (int)start.x;
        while(checkBlack(mat.get((int) ((slope * getOutBlack) + yIntercept),getOutBlack))){
            getOutBlack+=1;
        }
        for (int i = (int) getOutBlack; i < (end.x + .5); i++) {
            double[] point = mat.get((int) ((slope * i) + yIntercept), i);
            Log.e("coordinate", "Coor: (" + i + ", " + (int) ((slope * i) + yIntercept) + ")");
            if (checkBlack(point)) {
                if (startBlack == -1) {
                    startBlack = i;
                } else {
                }
            } else {
                if (startBlack == -1) {

                } else {
                    if(i-startBlack>10){
                        int blackX = (startBlack + ((i - startBlack) / 2));
                        if (points.size()>=1){
                            points.add(new Point(((points.get(points.size()-1).x+blackX)/2),(slope * ((points.get(points.size()-1).x+blackX)/2)) + yIntercept));
                        }
                        points.add(new Point(blackX,(slope * i) + yIntercept));
                    }
                    startBlack = -1;       }
            }
        }
        return points;
    }


    public List<Point> findTimingVerticle(Mat mat, Point start, Point end) {
        List<Point> points = new ArrayList<>();
        double rise = start.y - end.y;
        double run = start.x - end.x;
        double slope = rise / run;
        double yIntercept = start.y - (start.x * slope);
        int startBlack = -1;
        Log.e("Trace:", "From (" + start.x + ", " + start.y + ") " +
                "\nTo (" + end.x + ", " + end.y + ")" +
                "\nRise:" + rise + "" +
                "\nRun: " + run + "" +
                "\nSlope" + slope +
                "\nY-intercept" + yIntercept);
        int getOutBlack = (int)start.y;
        while(checkBlack(mat.get(getOutBlack,(int)((getOutBlack - yIntercept) / slope)))){
            getOutBlack+=1;
        }

        for (int i = getOutBlack; i < (end.y + .5); i++) {
            double[] point = mat.get(i, (int) ((i - yIntercept) / slope));
            Log.e("coordinate", "Coor: (" + i + ", " + ((int) (i - yIntercept) / slope) + ")");
            if (checkBlack(point)) {
                if (startBlack == -1) {
                    startBlack = i;
                } else {
                }
            } else {
                if (startBlack == -1) {
                } else {
                    if(i-startBlack>10){
                        int blackY = startBlack + ((i - startBlack) / 2);
                        if (points.size()>=1){
                            points.add(new Point(((((points.get(points.size()-1).y+blackY)/2)) - yIntercept) / slope,(points.get(points.size()-1).y+blackY)/2));
                        }
                        Log.e("size","i:"+(i-startBlack));
                        points.add(new Point(((int) (blackY - yIntercept) / slope), blackY));
                    }
                    startBlack = -1;
                }
            }
        }
        return points;
    }

    public boolean checkBlack(double[] d) {
        if (d != null) {
            if (d.length == 4) {
                double[] valMin = new double[4];
                double[] valMax = new double[4];

                valMin[0] = 0;//minH;
                valMax[0] = 180;//maxH;
                valMin[1] = 0;//hsvColor.val[1] - mColorRadius.val[1];
                valMax[1] = 255;//hsvColor.val[1] + mColorRadius.val[1];
                valMin[2] = 0;//hsvColor.val[2] - mColorRadius.val[2];
                valMax[2] = 30;//hsvColor.val[2] + mColorRadius.val[2];
                valMin[3] = 0;
                valMax[3] = 255;

                for (int i = 0; i < 4; i++) {
                    if (valMin[i] <= d[i] && valMax[i] >= d[i]) {

                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
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
public void safeMove(View view, float Xset, float Yset){
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    if(Xset<0
            ||Yset<0
            ||Xset+view.getWidth()>=metrics.widthPixels
            ||Yset+view.getHeight()>=metrics.heightPixels){

    }else{
        view.setX(Xset);
        view.setY(Yset);
    }
}

}