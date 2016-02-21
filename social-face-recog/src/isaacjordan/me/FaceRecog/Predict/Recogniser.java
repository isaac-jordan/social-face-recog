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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
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
import org.bytedeco.javacv.DC1394FrameGrabber;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FlyCaptureFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

import isaacjordan.me.FaceRecog.SocialFeed;
import isaacjordan.me.FaceRecog.TwitterFeed;

import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import org.bytedeco.javacpp.helper.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class Recogniser implements Runnable {
	FaceRecognizer faceRecognizer;
	Map<Integer, String> idToName;
	Map<Integer, Map<String, SocialFeed>> idToSocialFeeds;

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
		
		@SuppressWarnings("deprecation")
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

		System.out.println("Training face recogniser.");
		faceRecognizer.train(images, labels);
		System.out.println("Finished training.");
	}
	
	private void readSocialFeedMapFromFile() {
		FileInputStream fis;
		try {
			fis = new FileInputStream("socialfeeds.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
	        idToSocialFeeds = (Map<Integer, Map<String, SocialFeed>>) ois.readObject();
	        ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	@Override
	public void run() {
		
		List<String> necessary_names = new ArrayList<String>();
		File conf = new File("names.conf");
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new FileReader(conf));
		    String text = null;
		    while ((text = reader.readLine()) != null) {
		    	necessary_names.add(text);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (reader != null) {
		            reader.close();
		        }
		    } catch (IOException e) {
		    }
		}
		
		List<Scalar> list_of_colours = new ArrayList<Scalar>();
		list_of_colours.add( new Scalar (255,0,0,1) );
		list_of_colours.add( new Scalar (0,0,255,1) );
		list_of_colours.add( new Scalar (255,255,0,1) );
		list_of_colours.add( new Scalar (255,0,255,1) );
		list_of_colours.add( new Scalar (0,255,255,1) );
		list_of_colours.add( new Scalar (255,20,147,1) );
		list_of_colours.add( new Scalar (255,193,37,1) );
		list_of_colours.add( new Scalar (0,190,240,1) );
		list_of_colours.add( new Scalar (124,255,30,1) );
		list_of_colours.add( new Scalar (20,255,148,1) );
				
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
		
		readSocialFeedMapFromFile();
		
		for (int userid : idToSocialFeeds.keySet()) {
			for (SocialFeed feed : idToSocialFeeds.get(userid).values()) {
				feed.updateLatestPosts();
			}
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
		CanvasFrame frame = new CanvasFrame("Facial Recogniser", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		
		Frame videoFrame;
		Mat videoMat = new Mat();
		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
		CascadeClassifier face_cascade = new CascadeClassifier("data/haarcascades/haarcascade_frontalface_alt.xml");
		
		FrameRateCounter fpsCounter = new FrameRateCounter();
		
		try {
			while (frame.isVisible() && (videoFrame = grabber.grab()) != null) {
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
	            
	            Mat resizedVideoMat = videoMat.clone();
	            resize(resizedVideoMat, resizedVideoMat, new opencv_core.Size(1280, 960));
	            List<String> recognised = new ArrayList<String>();	            
	            
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

	                // Create the text we will annotate the box with:
	                String box_text = null;
	                String latestTweet = "";
	                if (prediction == -1 || distance > 60) {
	                	box_text = "HACKER";
	                	prediction = 0;
	                } else {
	                	box_text = idToName.get(prediction);
	                	Map<String, SocialFeed> socialFeeds = idToSocialFeeds.get(prediction);
	                	if (socialFeeds != null) {
	                		TwitterFeed feed = (TwitterFeed) socialFeeds.get("twitter");
	                		if (feed != null) {
	                			latestTweet = feed.getLatestPosts().get(0).getSummary();
	                		}
	                	}
	                	recognised.add(box_text);
	                }
	                
	                int j = prediction % 10;
	                
	                // And finally write all we've found out to the original image!
	                // First of all draw a green rectangle around the detected face:
	                face_i = new Rect(face_i.x() * 2, face_i.y() * 2, face_i.width() * 2, face_i.height() * 2);
	                rectangle(resizedVideoMat, face_i, list_of_colours.get(j));
	                
	                
	                // Calculate the position for annotated text (make sure we don't
	                // put illegal values in there):
	                int pos_x = Math.max(face_i.tl().x() - 10, 0);
	                int pos_y = Math.max(face_i.tl().y() - 10, 0);
	                
	                // And now put it into the image:
	                putText(resizedVideoMat, box_text, new Point(pos_x, pos_y),
	                        FONT_HERSHEY_PLAIN, 1.0, list_of_colours.get(j));
	                
	                List<String> lines_of_tweets = new ArrayList<String>();
	                String line = "";
	                StringBuilder sb = new StringBuilder(line);
	                int char_counter = 0;
	                for (char single_char : latestTweet.toCharArray()) {
	                	if (char_counter > 20 && (single_char == ' ' || single_char == '.') || char_counter > 35) {
	                		lines_of_tweets.add(sb.toString());
	                		sb = new StringBuilder(line);
	                		char_counter = 0;
	                	} else {
		                	sb.append(single_char);	
	                	}
	                	char_counter++;
	                }
	                if (sb.length() > 0) {
	                	lines_of_tweets.add(sb.toString());
	                }
	                int new_line = 0;
	                
	                for (String tweet_line : lines_of_tweets) {
	                	putText(resizedVideoMat, tweet_line, new Point(pos_x + face_i.width() + 14, pos_y + 25 + new_line),
		                        FONT_HERSHEY_PLAIN, 1, list_of_colours.get(j));
	                	new_line += 20;
	                }
	            }
	            
	            int number_required = necessary_names.size();
	            int number_got = 0;
	            
	            for (String rec_name : recognised) {
	            	if (necessary_names.contains(rec_name)) {
	            		number_got++;
	            	}
	            }
	            if (number_required == number_got) {
	            	System.out.println("Success");
	            	rectangle(videoMat, new Point(15, 15), new Point(videoMat.size().width() - 15, videoMat.size().height() - 15), (0, 255, 0, 255), 3); 
	            }
	            
	            
	            putText(resizedVideoMat, "FPS: " + fpsCounter.getFrameRate(), new Point(25, 25),
                        FONT_HERSHEY_PLAIN, 1.0, new Scalar(0, 255, 0, 2.0));
	            
	            // Show the result:
	            
	            IplImage image = new IplImage(resizedVideoMat);
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
