
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Vision {
	String path;
	Mat img;
	private static final int IMG_SCALE = 2;
	private int hueLower = 85, hueUpper = 255, satLower = 85, satUpper = 255, lumLower = 100, lumUpper= 255;
	private Mat hsv;
	private Mat imgThreshold;

	private Point cogPt;
	private int contourAxisAngle;
	private ArrayList fingerTips;
	ArrayList<Rect> array = new ArrayList<Rect>();
	public Vision(String path) {
		this.path = path;

	}

	public void process() {
		array = detection_contours(convertToHSV());
		if (array.size() > 0) {
			 
            Iterator<Rect> it2 = array.iterator();
            while (it2.hasNext()) {
                Rect obj = it2.next();
                Imgproc.rectangle(img, obj.br(), obj.tl(),
                        new Scalar(0, 255, 0), 1);
            }

        }
		Main.showImg(img);
		
	}

	public Mat convertToHSV() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		img = Imgcodecs.imread(path);

		hsv = new Mat();
		Mat kernel = Mat.ones(new Size(1, 1), CvType.CV_8U);

		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV);
		Core.inRange(hsv, new Scalar(85, 85, 100), new Scalar(255, 255, 255), hsv);
		Imgproc.erode(hsv, hsv, kernel, new Point(0, 0), 6);
		Imgproc.dilate(hsv, hsv, kernel, new Point(0, 0), 6);
		Main.showImg(hsv);
		return hsv;
	}
	
	public static ArrayList<Rect> detection_contours(Mat outmat) {
        Mat img = new Mat();
        Mat outputImg = outmat.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(outputImg, contours, outputImg, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
 
        double maxArea = 100;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();
 
        for (int idx = 0; idx < contours.size(); idx++) {
        	Mat contour = contours.get(idx); double contourarea = Imgproc.contourArea(contour); if (contourarea > maxArea) {
                maxAreaIdx = idx;
                r = Imgproc.boundingRect(contours.get(maxAreaIdx));
                rect_array.add(r);
            }
 
        }
 
        img.release();
 
        return rect_array;
 
    }
	
	
	
	
	/*differnt methods, that do not work (yet)*/
	public void update(String path) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat img = Imgcodecs.imread(path);
		Mat hsvImg  = new Mat();
		//Imgproc.resize(img, img, new Size(img.height() / IMG_SCALE, img.width() / IMG_SCALE));
		Imgproc.cvtColor(img, hsvImg, Imgproc.COLOR_RGB2HSV);
		Core.inRange(hsvImg, new Scalar(85, 85, 100), new Scalar(255, 255, 255),
				hsvImg);

		Mat kernel = Mat.ones(new Size(1, 1), CvType.CV_8U);

		Imgproc.erode(hsvImg, hsvImg, kernel, new Point(0, 0), 6);
		Imgproc.dilate(hsvImg, hsvImg, kernel, new Point(0, 0), 6);

		MatOfPoint bigContour = findBiggestContour(hsvImg);
		if (bigContour == null)
			return;
		System.out.println(bigContour);
		extractContourInfo(bigContour, IMG_SCALE);

		findFingerTips(bigContour, IMG_SCALE);

		//nameFingers(cogPt, contourAxisAngle, fingerTips);

	}

	private static final float SMALLEST_AREA = 600.0f;
	int biggestContourPts;
	int currentContour;
	MatOfPoint biggestContour;
	// private CvMemStorage contourStorage;

	public MatOfPoint findBiggestContour(Mat hsvImg) {
		
		
		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(hsvImg, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++) { // for each contour
			
			
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());

			double approxDistance = Imgproc.arcLength(contour2f, true) * .02; // measure the length of a closed contour
																				// curve
			Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true); // connect the points of the contours to approximate a closed polygon

			MatOfPoint points = new MatOfPoint(approxCurve.toArray()); // convert back to matofpoint
			currentContour = points.height();
			if(currentContour > biggestContourPts) {
				biggestContourPts= currentContour;
				biggestContour = contours.get(i);
			}
			/*
				Rect rect = Imgproc.boundingRect(points); // create bounding box
				int x = rect.x;
				int y = rect.y;
				int height = rect.height;
				int width = rect.width;
				Imgproc.rectangle(hsvImg, new Point(rect.x, rect.y),
						new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 255, 255), 3);// draw the rect
		*/
		}
		return biggestContour;
	}
	
	private void extractContourInfo(MatOfPoint bigContour, int scale) {
	//	CvMoments moments = new CvMoments();
	}
	
	public void findFingerTips(MatOfPoint bigContour, int scale) {
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		//double approxDistance = Imgproc.arcLength(contour2f, true) * .02;
	//	MatOfPoint approxContour = Imgproc.approxPolyDP(bigContour, approxCurve, approxDistance, closed);
	}
	
	
	
	/*
	 * public Mat convertToGrayToHOG(Mat img) { Imgproc.cvtColor(img, img,
	 * Imgproc.COLOR_RGB2GRAY); //Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2); }
	 * 
	 * public Mat cropImg(Mat img, int x, int y, int w, int h, int resizeW, int
	 * resizeH) { //Imgproc.rectangle(img, new Point(x1, y1), new Point(x2, y2),
	 * scalar); Rect crpImg = new Rect(x, y, w, h); img = img.submat(crpImg);
	 * Imgproc.resize(img, img, new Size(resizeW, resizeH));
	 * 
	 * return img; }
	 * 
	 * public void getData(String user_list, String[][] img_dict, String data_dir) {
	 * ArrayList<String> x = new ArrayList<String>(); ArrayList<String> y = new
	 * ArrayList<String>(); ArrayList<String>userImages = new ArrayList<String>();
	 * List<String[]> rowList = new ArrayList<String[]>();
	 * 
	 * File dir = new File(user_list); File[] directoryListing = dir.listFiles(); if
	 * (directoryListing != null) { for (File user : directoryListing) { String
	 * fileName = user.getName(); userImages.add(data_dir + fileName + "/*.jpg");
	 * try(BufferedReader br = new BufferedReader(new FileReader(data_dir + fileName
	 * + "/" + fileName + "_loc.csv"))){ String line; while((line = br.readLine())
	 * != null){ String[] lineItems = line.split(","); rowList.add(lineItems); }
	 * br.close(); } catch(Exception e) { System.out.println(e); } String[][] matrix
	 * = new String[rowLisst.size()][]; for(int i = 0; i < rowList.size(); i++){
	 * String[] row = rowList.get(i); matrix[i] = row; } for(int i = 0; i <
	 * rowList.size(); i++) { Mat cropped_img =
	 * cropImg(img_dict(Mat)([rowList.get(i)]["image"]),
	 * img_dict[rowList.get(i)]["top_left_x"],
	 * img_dict[rowList.get(i)]["bottom_right_x"],
	 * img_dict[rowList.get(i)]["top_left_x"],
	 * img_dict[rowList.get(i)]["bottom_right_y"], 128);
	 * 
	 * Mat
	 * 
	 * } } }
	 */


}
