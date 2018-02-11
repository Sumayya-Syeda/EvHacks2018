import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class BackProjection {
	Mat src, hsv, hue;
	int bins = 25;
	String path;
	public BackProjection(String path) {
		this.path = path;
		
	}
	public void convertToHsv(String path) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		src = Imgcodecs.imread(path);
		
		Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);
		
		hue.create(hsv.size(), hsv.depth());
		int ch[] = {0,0};
		//Core.mixChannels(hsv, 1, hue, 1, ch, 1);
		
		//char* window_image = "source img";
		Hist_and_Backproj(0,0);
		
		Main.showImg(src);
		
	}
	
	public void Hist_and_Backproj(int x, int y) {
		//MatND hist;
		try{
		
		
		float hue_range[] = {0, 180};
		//const float ranges = {hue_ranges};
		
		List<Mat> imagesList = new ArrayList<>();
		imagesList.add(hsv);
		int channelArray[] = {0,1, 2};
		MatOfInt channels = new MatOfInt(channelArray);
		MatOfInt histSize = new MatOfInt(Math.max(bins, 2));
		MatOfFloat ranges = new MatOfFloat(hue_range);
		Mat hist = new Mat();
		
		//get hist, normalize it
		Imgproc.calcHist(imagesList, channels, new Mat(), hist, histSize, ranges);
		Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX, -1, new Mat());

		//get back projection
		Mat backproj = new Mat();
		Imgproc.calcBackProject(imagesList, channels, hist, backproj, ranges, 1);
		
		//draw hist
		int w = 400;
		int h = 400;
		int bin_w = (int)(Math.round((double)(w/bins)));
		Mat histImg = Mat.zeros(w, h, CvType.CV_8UC3);
		
		for(int i = 0; i < bins; i++) {
			Imgproc.rectangle(histImg, new Point(i*bin_w, h),
							new Point((i+1)*bin_w, h-Math.round(hist.get(i,0)[0])), new Scalar(0,0,255), -1);
		}
		
		Main.showImg(histImg);
		
		
		}catch(Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void drawHist() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat img = Imgcodecs.imread(path);
		Mat rgba = img;
		
		int histSize = 256;
		MatOfInt histogramSize = new MatOfInt(histSize);
		
		int histogramHeight = (int) rgba.height();
		int binWidth = 5;
		
		MatOfFloat histogramRange = new MatOfFloat(0f, 256f);
		
		Scalar[] colorsRgb = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
		MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
		
		Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};
		Mat histMatBitmap = new Mat(histogramHeight);
	
		for (int i = 0; i < channels.length; i++) {
		    Imgproc.calcHist(Collections.singletonList(rgba), channels[i], new Mat(), histograms[i], histogramSize, histogramRange);
		    Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
		    for (int j = 0; j < histSize; j++) {
		        Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[i].get(j - 1, 0)[0]));
		        Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[i].get(j, 0)[0]));
		        Imgproc.line(histMatBitmap, p1, p2, colorsRgb[i], 2, 8, 0);
		    }
		}
		
	}
	
	
}
