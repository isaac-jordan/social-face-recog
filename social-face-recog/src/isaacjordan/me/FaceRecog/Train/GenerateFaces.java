package isaacjordan.me.FaceRecog.Train;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FrameGrabber.Exception;

import isaacjordan.me.FaceRecog.GitHubFeed;
import isaacjordan.me.FaceRecog.SocialFeed;
import isaacjordan.me.FaceRecog.TwitterFeed;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.indexer.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

class FaceGrabber implements Runnable {
	final int INTERVAL=20;
	IplImage image;
	Map<Integer, Map<String, SocialFeed>> idToSocialFeeds = null;
	Scanner scanner;
	
	//CanvasFrame canvas = new CanvasFrame("Web Cam");

	public FaceGrabber() {
		readSocialFeedMapFromFile();
		if (idToSocialFeeds == null)
			idToSocialFeeds = new HashMap<>();
	}
	
	private void gatherSocialFeeds(int userid) {
		System.out.println("Twitter username? Type 'null' to skip.");
		String username=scanner.nextLine();
		if (!username.equals("null"))
			idToSocialFeeds.get(userid).put("twitter", new TwitterFeed(username));
		
		System.out.println("GitHub username? Type 'null' to skip.");
		username=scanner.nextLine();
		if (!username.equals("null"))
			idToSocialFeeds.get(userid).put("github", new GitHubFeed(username));
		System.out.println("Finished gathering social feeds.");
	}
	
	private void readSocialFeedMapFromFile() {
		FileInputStream fis;
		try {
			fis = new FileInputStream("socialfeeds.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        idToSocialFeeds = (Map<Integer, Map<String, SocialFeed>>) ois.readObject();
	        ois.close();
		} catch (FileNotFoundException e) {
			System.out.println("No existing social feed files found. A new one will be created.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
        
	}
	
	private Map<String, SocialFeed> getOrAddNewUserFeed(int userid) {
		if (idToSocialFeeds.get(userid) != null) {
			return idToSocialFeeds.get(userid);
		}
		HashMap<String, SocialFeed> newFeedMap = new HashMap<>();
		idToSocialFeeds.put(userid, newFeedMap);
		return newFeedMap;
	}
	
	private void writeFeedDetailsToFile() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("socialfeeds.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(idToSocialFeeds);
	        oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		scanner = new Scanner(System.in);
        int id;
        String name;
        System.out.println("Enter user id:");
        id=scanner.nextInt();
        scanner.nextLine(); //This is needed to pick up the new line
        System.out.println("Enter user name:");
        name=scanner.nextLine();
        
        getOrAddNewUserFeed(id);
        
        gatherSocialFeeds(id);
        writeFeedDetailsToFile();
		String classifierName = null;
		URL url = null;
		try {
			url = new URL(
					"https://raw.github.com/Itseez/opencv/2.4.0/data/haarcascades/haarcascade_frontalface_alt.xml");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = null;
		try {
			file = Loader.extractResource(url, null, "classifier", ".xml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		file.deleteOnExit();
		classifierName = file.getAbsolutePath();

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		// We can "cast" Pointer objects by instantiating a new object of the
		// desired class.
		CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
		if (classifier.isNull()) {
			System.err.println("Error loading classifier file \"" + classifierName + "\".");
			System.exit(1);
		}
		
		System.out.println("Press enter to begin frame capture.");
        scanner.nextLine();

		// The available FrameGrabber classes include OpenCVFrameGrabber
		// (opencv_videoio),
		// DC1394FrameGrabber, FlyCaptureFrameGrabber, OpenKinectFrameGrabber,
		// PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
		FrameGrabber grabber = null;
		try {
			grabber = FrameGrabber.createDefault(0);
			grabber.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// CanvasFrame, FrameGrabber, and FrameRecorder use Frame objects to
		// communicate image data.
		// We need a FrameConverter to interface with other APIs (Android, Java
		// 2D, or OpenCV).
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

		// FAQ about IplImage and Mat objects from OpenCV:
		// - For custom raw processing of data, createBuffer() returns an NIO
		// direct
		// buffer wrapped around the memory pointed by imageData, and under
		// Android we can
		// also use that Buffer with Bitmap.copyPixelsFromBuffer() and
		// copyPixelsToBuffer().
		// - To get a BufferedImage from an IplImage, or vice versa, we can
		// chain calls to
		// Java2DFrameConverter and OpenCVFrameConverter, one after the other.
		// - Java2DFrameConverter also has static copy() methods that we can use
		// to transfer
		// data more directly between BufferedImage and IplImage or Mat via
		// Frame objects.
		IplImage grabbedImage = null;
		try {
			grabbedImage = converter.convert(grabber.grab());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int width = grabbedImage.width();
		int height = grabbedImage.height();
		IplImage grayImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);

		// Objects allocated with a create*() or clone() factory method are
		// automatically released
		// by the garbage collector, but may still be explicitly released by
		// calling release().
		// You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on
		// objects allocated this way.
		CvMemStorage storage = CvMemStorage.create();

		// CanvasFrame is a JFrame containing a Canvas component, which is
		// hardware accelerated.
		// It can also switch into full-screen mode when called with a
		// screenNumber.
		// We should also specify the relative monitor/camera response for
		// proper gamma correction.
		CanvasFrame frame = new CanvasFrame("Face File Generator", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		
		int count = 0;
		int framecounter = 0;
		Size newImageSize = new Size(250,250);
		
		System.out.println(String.format("Taking screenshot every %d frames.", INTERVAL));
		try {
			while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
				cvClearMemStorage(storage);

				// Let's try to detect some faces! but we need a grayscale image...
				cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
				
				CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3,
						CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH);
				
				int total = faces.total();
				
				IplImage drawnImage = grabbedImage.clone();
				for (int i = 0; i < total; i++) {
					CvRect r = new CvRect(cvGetSeqElem(faces, i));
					int x = r.x(), y = r.y(), w = r.width(), h = r.height();
					cvRectangle(drawnImage, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);
				}

				Frame convertedFrame = converter.convert(drawnImage);
				frame.showImage(convertedFrame);
				
				if (framecounter >= INTERVAL) {
					if (total == 1) {
						String filename = null;
						while (true) {
							filename = String.format("%s%d-%s-%d.png", 
									"images/",
									id, name, count++);
							File f = new File(filename);
							if(f.exists() && !f.isDirectory()) { 
							    count++;
							} else {
								break;
							}
						}
						
					    System.out.println(String.format("Writing %s", filename));
					    
					    CvRect r = new CvRect(cvGetSeqElem(faces, 0));
					    int x = r.x(), y = r.y(), w = r.width(), h = r.height();
					    cvSetImageROI(grabbedImage, cvRect(x+1, y+1, w-1, h-1));
					    Frame croppedFrame = converter.convert(grabbedImage);
					    
					    IplImage resizeImage = IplImage.create(250, 250, grabbedImage.depth(), grabbedImage.nChannels());
					    cvResize(grabbedImage, resizeImage);
					    cvSaveImage(filename, resizeImage);
					}
					
					grabber.stop();
					Thread.sleep(75);
					grabber.restart();
					framecounter = 0;
				}
				framecounter++;
			}
		} catch (Exception | InterruptedException e) {
			e.printStackTrace();
		}
		
		frame.dispose();
		
		try {
			grabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
        scanner.close();
	}
}


public class GenerateFaces {
  public static void main(String[] args) {
    new FaceGrabber().run();
  }
}