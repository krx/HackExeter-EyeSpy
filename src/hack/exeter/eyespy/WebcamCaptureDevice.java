package hack.exeter.eyespy;

import java.awt.Dimension;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/**
 * @author Kareem ElFaramawi
 */
public class WebcamCaptureDevice {
	public static final int DEFAULT_IDX = 0;
	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 480;
	
	private VideoCapture camera;
	private Mat img = new Mat();
	
	public WebcamCaptureDevice() {
		this( DEFAULT_IDX, DEFAULT_WIDTH, DEFAULT_HEIGHT );
	}
	
	public WebcamCaptureDevice( int index ) {
		this( index, DEFAULT_WIDTH, DEFAULT_HEIGHT );
	}
	
	public WebcamCaptureDevice( int width, int height ) {
		this( DEFAULT_IDX, width, height );
	}
	
	public WebcamCaptureDevice( int index, int width, int height ) {
		camera = new VideoCapture();
		camera.open( index );
		camera.set( Highgui.CV_CAP_PROP_FRAME_WIDTH, width );
		camera.set( Highgui.CV_CAP_PROP_FRAME_HEIGHT, height );
		camera.retrieve( img );
	}
	
	public Dimension getImageSize() {
		return new Dimension( (int) camera.get( Highgui.CV_CAP_PROP_FRAME_WIDTH ), (int) camera.get( Highgui.CV_CAP_PROP_FRAME_HEIGHT ) );
	}
	
	public Mat getImage() {
		camera.retrieve( img );
		Core.flip( img, img, 1 );
		return img;
	}
	
	public void close() {
		camera.release();
	}
}
