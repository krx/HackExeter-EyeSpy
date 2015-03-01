package hack.exeter.eyespy;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;

import static hack.exeter.eyespy.Constants.*;
import static hack.exeter.eyespy.EyeCenter.*;

/**
 * @author Kareem ElFaramawi, Ankur Sundara
 */
@SuppressWarnings( "serial" )
public class Tracker extends JPanel implements KeyListener {
	static WebcamCaptureDevice captureDevice;
	final String face_cascade_name = "C:/opencv/sources/data/haarcascades/haarcascade_frontalface_alt.xml";
	private CascadeClassifier face_cascade;
	
	BufferedImage img_faceROI;
	BufferedImage img_frame;
	
	Mat frame;
	Mat faceROI;
	
	Rect leftEyeRegion;
	Rect rightEyeRegion;

	Point leftPupil;
	Point rightPupil;
	
	public Tracker() {
		captureDevice = new WebcamCaptureDevice();
		setPreferredSize( captureDevice.getImageSize() );
		face_cascade = new CascadeClassifier();
		
		leftEyeRegion = new Rect();
		rightEyeRegion = new Rect();
		
		leftPupil = new Point();
		rightPupil = new Point();
		
		// Load the cascades
		if ( !face_cascade.load( face_cascade_name ) ) {
			System.out.println( "Error loading face cascade" );
		}
		
		Dimension dim = captureDevice.getImageSize();
		setPreferredSize( new Dimension( dim.width * 2, dim.height ) );
		
		setFocusable( true );
		addKeyListener( this );
	}
	
	public void update() {
		frame = captureDevice.getImage();
		detectFaceAndEyes( frame );
		if ( frame != null ) {
			img_frame = MatToImg( frame );
		}
		if ( faceROI != null ) {
			img_faceROI = MatToImg( faceROI );
		}
		repaint();
	}
	
	public WebcamCaptureDevice getCaptureDevice() {
		return captureDevice;
	}
	
	private static BufferedImage MatToImg( Mat mat ) {
		MatOfByte bytes = new MatOfByte();
		Highgui.imencode( ".png", mat, bytes );
		BufferedImage img = null;
		try {
			img = ImageIO.read( new ByteArrayInputStream( bytes.toArray() ) );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return img;
	}
	
	@Override
	protected void paintComponent( Graphics g ) {
		g.fillRect( 0, 0, getWidth(), getHeight() );
		if ( img_frame != null ) {
			g.drawImage( img_frame, 0, 0, null );
		}
		if ( img_faceROI != null ) {
			g.drawImage( img_faceROI, img_frame.getWidth(), 0, null );
		}
	}
	
	void findEyes( Mat frame_gray, Rect face ) {
		faceROI = frame_gray.submat( face );
		Mat debugFace = faceROI;
		
		// Find eye regions and draw them
		int eye_region_width = (int) ( face.width * ( EYE_PERCENT_WIDTH / 100.0 ) );
		int eye_region_height = (int) ( face.width * ( EYE_PERCENT_HEIGHT / 100.0 ) );
		int eye_region_top = (int) ( face.height * ( EYE_PERCENT_TOP / 100.0 ) );
		leftEyeRegion = new Rect( (int) ( face.width * ( EYE_PERCENT_SIDE / 100.0 ) ), eye_region_top, eye_region_width, eye_region_height );
		rightEyeRegion = new Rect( (int) ( face.width - eye_region_width - face.width * ( EYE_PERCENT_SIDE / 100.0 ) ), eye_region_top, eye_region_width, eye_region_height );
		
		// Find Eye Centers
		leftPupil = findEyeCenter( faceROI, leftEyeRegion);
		rightPupil = findEyeCenter( faceROI, rightEyeRegion);
		rightPupil.x += rightEyeRegion.x;
		rightPupil.y += rightEyeRegion.y;
		leftPupil.x += leftEyeRegion.x;
		leftPupil.y += leftEyeRegion.y;
		
		// Draw eye centers
		Core.circle( debugFace, rightPupil, 3, new Scalar( 1234 ) );
		Core.circle( debugFace, leftPupil, 3, new Scalar( 1234 ) );
		
	}
	
	void detectFaceAndEyes( Mat frame ) {
		MatOfRect faces = new MatOfRect();
		ArrayList<Mat> rgbChannels = new ArrayList<Mat>( 3 );
		Core.split( frame, rgbChannels );
		Mat frame_gray = rgbChannels.get( 2 );
		face_cascade.detectMultiScale( frame_gray, faces, 1.1, 2, 0, new Size( 150, 150 ), new Size() );
		Rect[] facesArray = faces.toArray();
		
		if ( facesArray.length > 0 ) {
			findEyes( frame_gray, facesArray[0] );
		}
	}
	
	public int getFaceWidth() {
		return img_faceROI == null ? 0 : img_faceROI.getWidth();
	}
	
	public int getFaceHeight() {
		return img_faceROI == null ? 0 : img_faceROI.getHeight();
	}
	
	public Rect getLeftEyeRegion() {
		return leftEyeRegion;
	}
	
	public Rect getRightEyeRegion() {
		return rightEyeRegion;
	}
	
	public Point getLeftPupil() {
		return leftPupil;
	}
	
	public Point getRightPupil() {
		return rightPupil;
	}
	
	@Override
	public void keyTyped( KeyEvent e ) {
		
	}
	
	public boolean space = false;
	
	@Override
	public void keyPressed( KeyEvent e ) {
		space = e.getKeyCode() == KeyEvent.VK_SPACE;
	}
	
	@Override
	public void keyReleased( KeyEvent e ) {
	}
	
}
