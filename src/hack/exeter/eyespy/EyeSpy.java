package hack.exeter.eyespy;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Eye and pupil tracking using Fabian Timm's Algorithm.
 * Paper covering this algorithm is found here: http://www.inb.uni-luebeck.de/publikationen/pdfs/TiBa11b.pdf
 * Blog post we used for some help and reference: http://thume.ca/projects/2012/11/04/simple-accurate-eye-center-tracking-in-opencv/
 * 
 * Depends on OpenCV Java Wrapper
 * 
 * @author Kareem El-Faramawi, Ankur Sundara, Bhanu Kappala, Sahil Shah 
 */
public class EyeSpy {
	static Tracker tracker;
	Robot robit;
	final static GraphicsDevice DEVICE = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	ArrayList<Double> sampleAvgX = new ArrayList<Double>();
	ArrayList<Double> sampleAvgY = new ArrayList<Double>();
	int maxAvgs = 10;
	
	Point leftCenter, rightCenter;
	
	int count = 0;
	
	public EyeSpy() {
		try {
			robit = new Robot();
		} catch ( AWTException e ) {
			e.printStackTrace();
		}
		
		new Thread() {
			public void run() {
				while ( true ) {
					tracker.update();
					if ( leftCenter != null ) {
						joystickControl();
						
					}
					
					if ( tracker.space ) {
						Point lp = tracker.getLeftPupil(), rp = tracker.getRightPupil();
						Rect lr = tracker.getLeftEyeRegion(), rr = tracker.getRightEyeRegion();
						leftCenter = new Point( ( lp.x - lr.x ) / lr.width, ( lp.y - lr.y ) / lr.height );
						rightCenter = new Point( ( rp.x - rr.x ) / rr.width, ( rp.y - rr.y ) / rr.height );
						System.out.println( "Calibrated at " + leftCenter + " and " + rightCenter );
						tracker.space = false;
						sampleAvgX.clear();
						sampleAvgY.clear();
					}
					//
					// if ( count == 2 ) {
					// eyeOriginControl();
					// }
					//
					// if ( tracker.space ) {
					// Point tll = tracker.getLeftPupil(), tlr = tracker.getRightPupil();
					// Rect lr = tracker.getLeftEyeRegion(), rr = tracker.getRightEyeRegion();
					// switch ( count ) {
					// case 0:
					// topLeftL = new Point( ( tll.x - lr.x ) / lr.width, ( tll.y - lr.y ) / lr.height );
					// topLeftR = new Point( ( tlr.x - rr.x ) / rr.width, ( tlr.y - rr.y ) / rr.height );
					// System.out.println( "Calibrated at " + topLeftL + " and " + topLeftR );
					// tracker.space = false;
					// System.out.println( "Calibrate to bottom right!" );
					// sampleAvgX.clear();
					// sampleAvgY.clear();
					//
					// count++;
					// break;
					// case 1:
					// bottomRightL = new Point( ( tll.x - lr.x ) / lr.width, ( tll.y - lr.y ) / lr.height );
					// bottomRightR = new Point( ( tlr.x - rr.x ) / rr.width, ( tlr.y - rr.y ) / rr.height );
					// System.out.println( "Calibrated at " + bottomRightL + " and " + bottomRightR );
					// tracker.space = false;
					// System.out.println( "Finihed Calibration!" );
					// sampleAvgX.clear();
					// sampleAvgY.clear();
					//
					// count++;
					// break;
					// }
					// }
					//
					Thread.yield();
				}
			}
		}.start();
	}
	
	double motX = 0;
	double motY = 0;
	int maxMot = 20;
	
	final double MOT_X_THRESH = 9.0;
	final double MOT_Y_THRESH = 9.0;
	
	public void joystickControl() {
		
		Point lp = tracker.getLeftPupil();
		Point rp = tracker.getRightPupil();
		Rect lr = tracker.getLeftEyeRegion();
		Rect rr = tracker.getRightEyeRegion();
		
		double lOffX = lp.x - lr.x - leftCenter.x * lr.width;
		double rOffX = rp.x - rr.x - rightCenter.x * rr.width;
		double xAvg = ( lOffX + rOffX ) / 2.0;
		
		double lOffY = lp.y - lr.y - leftCenter.y * lr.height;
		double rOffY = rp.y - rr.y - rightCenter.y * rr.height;
		double yAvg = ( lOffY + rOffY ) / 2.0;
		
		xAvg *= 5;
		yAvg *= 5;
		
		sampleAvgX.add( xAvg );
		if ( sampleAvgX.size() > maxAvgs ) {
			sampleAvgX.remove( 0 );
		}
		xAvg = avg( sampleAvgX );
		
		sampleAvgY.add( yAvg );
		if ( sampleAvgY.size() > maxAvgs ) {
			sampleAvgY.remove( 0 );
		}
		yAvg = avg( sampleAvgY );
		
		System.out.printf( "xAvg: %.2f\tyAvg: %.2f\n", xAvg, yAvg );
		
		if ( Math.abs( xAvg ) >= MOT_X_THRESH ) {
			motX = MathUtils.clamp( xAvg, -maxMot, maxMot );
		} else {
			motX = 0;
		}
		
		if ( Math.abs( yAvg ) >= MOT_Y_THRESH ) {
			motY = MathUtils.clamp( yAvg, -maxMot, maxMot );
		} else {
			motY = 0;
		}
		
		java.awt.Point m = MouseInfo.getPointerInfo().getLocation();
		robit.mouseMove( (int) ( m.x + motX ), (int) ( m.y + motY ) );
	}
	
	private double avg( ArrayList<Double> nums ) {
		if ( nums.size() == 0 ) {
			return 0;
		}
		double sum = 0;
		for ( double d : nums ) {
			sum += d;
		}
		return sum / nums.size();
	}
	
	// public void eyeOriginControl() {
	//
	// Point lp = tracker.getLeftPupil();
	// Point rp = tracker.getRightPupil();
	// Rect lr = tracker.getLeftEyeRegion();
	// Rect rr = tracker.getRightEyeRegion();
	//
	// double lOffX = ( ( lp.x - lr.x ) / lr.width - ( topLeftL.x + bottomRightL.x ) / 2 );
	// double rOffX = ( ( rp.x - rr.x ) / rr.width - ( topLeftR.x + bottomRightR.x ) / 2 );
	// double xAvg = ( lOffX + rOffX ) / 2.0;
	//
	// double lOffY = ( ( lp.y - lr.y ) / lr.height - ( topLeftL.y + bottomRightL.y ) / 2 );
	// double rOffY = ( ( rp.y - rr.y ) / rr.height - ( topLeftR.y + bottomRightR.y ) / 2 );
	// double yAvg = ( lOffY + rOffY ) / 2.0;
	//
	// sampleAvgX.add( xAvg );
	// if ( sampleAvgX.size() > maxAvgs ) {
	// sampleAvgX.remove( 0 );
	// }
	// xAvg = avg( sampleAvgX );
	//
	// sampleAvgY.add( yAvg );
	// if ( sampleAvgY.size() > maxAvgs ) {
	// sampleAvgY.remove( 0 );
	// }
	// yAvg = avg( sampleAvgY );
	//
	// // System.out.println( "xAvg: " + xAvg );
	// //
	// // System.out.println( "yAvg: " + yAvg );
	// //
	// // double xPct = xAvg / ( ( lr.width + rr.width ) / 2 );
	// // double yPct = yAvg / ( ( lr.height + rr.height ) / 2 );
	// // System.out.println( xPct );
	// //
	// // System.out.println("%x : " + xAvg/)
	//
	// java.awt.Point m = MouseInfo.getPointerInfo().getLocation();
	// java.awt.Rectangle screen = DEVICE.getDefaultConfiguration().getBounds();
	//
	// // robit.mouseMove( (int) ( screen.getCenterX() + xPct * screen.getWidth() ), (int) ( screen.getCenterY() + yPct * screen.getHeight() ) );
	// double avgTLX = ( topLeftL.x + topLeftR.x ) / 2;
	// double avgTLY = ( topLeftL.y + topLeftR.y ) / 2;
	// double avgBRX = ( bottomRightL.x + bottomRightR.x ) / 2;
	// double avgBRY = ( bottomRightL.y + bottomRightR.y ) / 2;
	// robit.mouseMove( (int) map( xAvg, avgTLX, avgBRX, 0, screen.width ), (int) map( yAvg, avgTLY, avgBRY, 0, screen.width ) );
	// }
	//
	// private double map( double val, double oldMin, double oldMax, double newMin, double newMax ) {
	// return val / ( oldMax - oldMin ) * ( newMax - newMin ) + newMin;
	// }
	
	public static void main( String[] args ) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		tracker = new Tracker();
		frame.add( tracker );
		frame.addWindowListener( new WindowAdapter() {
			public void windowClosing( java.awt.event.WindowEvent e ) {
				tracker.getCaptureDevice().close();
			};
		} );
		frame.pack();
		frame.setVisible( true );
		
		new EyeSpy();
	}
}
