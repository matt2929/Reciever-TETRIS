package org.opencv.samples.colorblobdetect;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import static android.content.ContentValues.TAG;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);
    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = .5;
    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25, 50, 50, 0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    public void setHsvBlack(Scalar hsvColor) {
        //were looking for black color given doesnt matter but its easier if one is given trust me
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0] - mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0] + mColorRadius.val[0] <= 255) ? hsvColor.val[0] + mColorRadius.val[0] : 255;
        mLowerBound.val[0] = 0;//minH;
        mUpperBound.val[0] = 180;//maxH;
        mLowerBound.val[1] = 0;//hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = 255;//hsvColor.val[1] + mColorRadius.val[1];
        mLowerBound.val[2] = 0;//hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = 30;//hsvColor.val[2] + mColorRadius.val[2];
        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;
        Mat spectrumHsv = new Mat(1, (int) (maxH - minH), CvType.CV_8UC3);
        for (int j = 0; j < maxH - minH; j++) {
            byte[] tmp = {(byte) (minH + j), (byte) 255, (byte) 255};
            spectrumHsv.put(0, j, tmp);
        }
        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }



    public void setHsvColor(Scalar hsvColor) {
        //set custom color to look for
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]-mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+mColorRadius.val[0] <= 255) ? hsvColor.val[0]+mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minH;
        mUpperBound.val[0] = maxH;

        mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];

        mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];

        mLowerBound.val[3] = 0;
        mUpperBound.val[3] = 255;

        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }


    public boolean process(Mat rgbaImage, Point point1, Point point2) {
//we finding contours
        Mat mat = rgbaImage.submat((int) Math.min(point1.y, point2.y), (int) Math.max(point1.y, point2.y), (int) Math.min(point1.x, point2.x), (int) Math.max(point1.x, point2.x));
        Imgproc.pyrDown(mat, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();

        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        mContours.clear();
        List<MatOfPoint> matOfPoints = new ArrayList<>();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
                Core.multiply(contour, new Scalar(4, 4), contour);
                contour=translateMatOfPoints(contour,new Point((int) Math.min(point1.x, point2.x),(int) Math.min(point1.y, point2.y)));
                mContours.add(contour);
            }
        }
        if(mContours.size()>0){
            return true;
        }else{
            return false;
        }
    }
    public static MatOfPoint translateMatOfPoints(MatOfPoint contour, Point translation)
    {
        org.opencv.core.Point[] points = contour.toArray();
        for (int i = 0; i < points.length; i++)
        {
            points[i].x += translation.x;
            points[i].y += translation.y;
        }
        contour.fromArray(points);
        return contour;
    }

    public static void setmMinContourArea(double mMinContourArea) {
        ColorBlobDetector.mMinContourArea = mMinContourArea;
    }

    public Point centerAverage()
    {
        int sumX=0,sumY=0;
        int count=0;
        for(int i=0;i<mContours.size();i++){
            org.opencv.core.Point[] points = mContours.get(i).toArray();
            for (int x = 0; x < points.length; x++)
            {
                sumX+=points[x].x;
                sumY+=points[x].y;
                count++;
            }
        }
        if (count==0){
            return new Point(-100,-100);
        }
        return new Point(sumX/count,sumY/count);
    }


    public List<MatOfPoint> getContours() {
        return mContours;
    }
}