package isaacjordan.me.FaceRecog.Predict;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class Recogniser implements Runnable {
	IplImage image;
	FaceRecognizer faceRecognizer;
	Map<Integer, String> idToName;

	public Recogniser() {
		String trainingDir = "images";
		File root = new File(trainingDir);

		FilenameFilter imgFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		MatVector images = new MatVector(imageFiles.length);

		Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
		IntBuffer labelsBuf = labels.getIntBuffer();

		int counter = 0;
		idToName = new HashMap<Integer, String>();
		for (File image : imageFiles) {
			Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

			String[] attributes = image.getName().split("-");
			int label = Integer.parseInt(attributes[0]);

			idToName.put(label, attributes[1]);
			images.put(counter, img);

			labelsBuf.put(counter, label);

			counter++;
		}

		// faceRecognizer = createFisherFaceRecognizer();
		// faceRecognizer = createEigenFaceRecognizer();
		faceRecognizer = createLBPHFaceRecognizer();

		System.out.println("Training face rocognizer.");
		faceRecognizer.train(images, labels);
		System.out.println("Finished training.");

	}
	
	@Override
	public void run() {
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		
		// The available FrameGrabber classes include OpenCVFrameGrabber
		// (opencv_videoio),
		// DC1394FrameGrabber, FlyCaptureFrameGrabber, OpenKinectFrameGrabber,
		// PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
		FrameGrabber grabber = null;
		try {
			grabber = FrameGrabber.createDefault(0);
			grabber.start();
		} catch (FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			System.err.println("FrameGrabber Exception!");
			e.printStackTrace();
		}
		

		// CanvasFrame, FrameGrabber, and FrameRecorder use Frame objects to
		// communicate image data.
		// We need a FrameConverter to interface with other APIs (Android, Java
		// 2D, or OpenCV).
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

		// CanvasFrame is a JFrame containing a Canvas component, which is
		// hardware accelerated.
		// It can also switch into full-screen mode when called with a
		// screenNumber.
		// We should also specify the relative monitor/camera response for
		// proper gamma correction.
		CanvasFrame frame = new CanvasFrame("Face File Generator", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		
		Frame videoFrame;
		Mat videoMat = new Mat();
		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
		CascadeClassifier face_cascade = new CascadeClassifier("data/haarcascades/haarcascade_frontalface_alt.xml");
		
		FrameRateCounter fpsCounter = new FrameRateCounter();
		
		try {
			while (frame.isVisible() && (videoFrame = grabber.grab()) != null) {
				//Record the time before update and draw
		        long beforeTime = System.nanoTime();
		        
	            videoMat = converterToMat.convert(videoFrame);
	            Mat videoMatGray = new Mat();
	            // Convert the current frame to grayscale:
	            cvtColor(videoMat, videoMatGray, COLOR_BGRA2GRAY);
	            equalizeHist(videoMatGray, videoMatGray);

	            Point p = new Point();
	            RectVector faces = new RectVector();
	            // Find the faces in the frame:
	            face_cascade.detectMultiScale(videoMatGray, faces);

	            // At this point you have the position of the faces in
	            // faces. Now we'll get the faces, make a prediction and
	            // annotate it in the video. Cool or what?
	            for (int i = 0; i < faces.size(); i++) {
	                Rect face_i = faces.get(i);

	                Mat face = new Mat(videoMatGray, face_i);
	                // If fisher face recognizer is used, the face need to be
	                // resized.
	                // resize(face, face_resized, new Size(im_width, im_height),
	                // 1.0, 1.0, INTER_CUBIC);

	                // Now perform the prediction, see how easy that is:
		            // pointer-like output parameters
		            // only the first element of these arrays will be changed
		            int[] plabel = new int[1];
		            plabel[0] = -1;
		            double[] pconfidence = new double[1];
		            faceRecognizer.predict(face, plabel, pconfidence);
	
		            int prediction = plabel[0];
		            double distance = pconfidence[0];

	                // And finally write all we've found out to the original image!
	                // First of all draw a green rectangle around the detected face:
	                rectangle(videoMat, face_i, new Scalar(0, 255, 0, 1));

	                // Create the text we will annotate the box with:
	                String box_text = null;
	                if (prediction == -1 || distance > 60) {
	                	box_text = "HACKER";
	                } else {
	                	box_text = idToName.get(prediction);
	                }
	                
	                // Calculate the position for annotated text (make sure we don't
	                // put illegal values in there):
	                int pos_x = Math.max(face_i.tl().x() - 10, 0);
	                int pos_y = Math.max(face_i.tl().y() - 10, 0);
	                
	                // And now put it into the image:
	                putText(videoMat, box_text, new Point(pos_x, pos_y),
	                        FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0, 2.0));
	                
	            }
	            
	            putText(videoMat, "FPS: " + fpsCounter.getFrameRate(), new Point(25, 25),
                        FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0, 2.0));
	            
	            // Show the result:
	            IplImage image = new IplImage(videoMat);
	            Frame convertedFrame = converter.convert(image);
				frame.showImage(convertedFrame);
				fpsCounter.submitReading();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		frame.dispose();
		
		try {
			grabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}