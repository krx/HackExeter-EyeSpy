package hack.exeter.eyespy;

import static hack.exeter.eyespy.Constants.ENABLE_WEIGHT;
import static hack.exeter.eyespy.Constants.FAST_EYE_WIDTH;
import static hack.exeter.eyespy.Constants.GRADIENT_THRESHOLD;
import static hack.exeter.eyespy.Constants.WEIGHT_BLUR_SIDES;
import static hack.exeter.eyespy.Constants.WEIGHT_DIVISOR;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * @author Kareem El-Faramawi
 */
public class EyeCenter {
	
	private EyeCenter() {}
	
	public static Point unscalePoint( Point p, Rect origSize ) {
		float ratio = ( ( (float) FAST_EYE_WIDTH ) / origSize.width );
		int x = (int) Math.round( p.x / ratio );
		int y = (int) Math.round( p.y / ratio );
		return new Point( x, y );
	}
	
	public static void scaleToFastSize( Mat src, Mat dst ) {
		Imgproc.resize( src, dst, new Size( FAST_EYE_WIDTH, ( ( (float) FAST_EYE_WIDTH ) / src.cols() ) * src.rows() ) );
	}
	
	public static Mat xGradient( Mat mat ) {
		Mat out = new Mat( mat.rows(), mat.cols(), CvType.CV_64F );
		for ( int y = 0; y < mat.rows(); ++y ) {
			Mat Mr = mat.row( y );
			Mat Or = out.row( y );
			
			Or.put( 0, 0, Mr.get( 0, 1 )[0] - Mr.get( 0, 0 )[0] );
			for ( int x = 1; x < mat.cols() - 1; ++x ) {
				Or.put( 0, x, ( Mr.get( 0, x + 1 )[0] - Mr.get( 0, x - 1 )[0] ) / 2.0 );
			}
			Or.put( 0, mat.cols() - 1, Mr.get( 0, mat.cols() - 1 )[0] - Mr.get( 0, mat.cols() - 2 )[0] );
		}
		
		return out;
	}
	
	public static void testPossibleCentersFormula( int x, int y, Mat weight, double gx, double gy, Mat out ) {
		for ( int cy = 0; cy < out.rows(); ++cy ) {
			Mat Or = out.row( cy );
			Mat Wr = weight.row( cy );
			for ( int cx = 0; cx < out.cols(); ++cx ) {
				if ( x == cx && y == cy ) {
					continue;
				}
				// Create a vector from the possible center to the gradient origin
				double dx = x - cx;
				double dy = y - cy;
				// Normalize d
				double magnitude = Math.sqrt( ( dx * dx ) + ( dy * dy ) );
				dx = dx / magnitude;
				dy = dy / magnitude;
				double dotProduct = dx * gx + dy * gy;
				dotProduct = Math.max( 0.0, dotProduct );
				// Square and multiply by the weight
				if ( ENABLE_WEIGHT ) {
					Or.put( 0, cx, Or.get( 0, cx )[0] + dotProduct * dotProduct * ( Wr.get( 0, cx )[0] / WEIGHT_DIVISOR ) );
				} else {
					Or.put( 0, cx, Or.get( 0, cx )[0] + dotProduct * dotProduct );
				}
			}
		}
	}
	
	public static Point findEyeCenter( Mat face, Rect eye ) {
		Mat eyeROIUnscaled = face.submat( eye );
		Mat eyeROI = new Mat();
		scaleToFastSize( eyeROIUnscaled, eyeROI );
		// Draw eye region
		Core.rectangle( face, eye.tl(), eye.br(), new Scalar( 1234 ) );
		// Find the gradient
		Mat gradientX = xGradient( eyeROI );
		Mat gradientY = xGradient( eyeROI.t() ).t();
		// Normalize and threshold the gradient
		// compute all the magnitudes
		Mat mags = AlgUtils.matrixMagnitude( gradientX, gradientY );
		// compute the threshold
		double gradientThresh = AlgUtils.dynamicThreshold( mags, GRADIENT_THRESHOLD );
		// normalize
		for ( int y = 0; y < eyeROI.rows(); ++y ) {
			Mat Xr = gradientX.row( y ), Yr = gradientY.row( y );
			Mat Mr = mags.row( y );
			for ( int x = 0; x < eyeROI.cols(); ++x ) {
				double gX = Xr.get( 0, x )[0], gY = Yr.get( 0, x )[0];
				double magnitude = Mr.get( 0, x )[0];
				if ( magnitude > gradientThresh ) {
					Xr.put( 0, x, gX / magnitude );
					Yr.put( 0, x, gY / magnitude );
				} else {
					Xr.put( 0, x, 0.0 );
					Yr.put( 0, x, 0.0 );
				}
			}
		}
		
		// Create a blurred and inverted image for weighting
		Mat weight = new Mat();
		Imgproc.GaussianBlur( eyeROI, weight, new Size( WEIGHT_BLUR_SIDES, WEIGHT_BLUR_SIDES ), 0, 0 );
		for ( int y = 0; y < weight.rows(); ++y ) {
			Mat row = weight.row( y );
			for ( int x = 0; x < weight.cols(); ++x ) {
				row.put( 0, x, 255 - row.get( 0, x )[0] );
			}
		}
		
		// Run the algorithm
		Mat outSum = Mat.zeros( eyeROI.rows(), eyeROI.cols(), CvType.CV_64F );
		for ( int y = 0; y < weight.rows(); ++y ) {
			Mat Xr = gradientX.row( y ), Yr = gradientY.row( y );
			for ( int x = 0; x < weight.cols(); ++x ) {
				double gX = Xr.get( 0, x )[0], gY = Yr.get( 0, x )[0];
				if ( gX == 0.0 && gY == 0.0 ) {
					continue;
				}
				testPossibleCentersFormula( x, y, weight, gX, gY, outSum );
			}
		}
		
		// Scale all the values down, basically averaging them
		double numGradients = ( weight.rows() * weight.cols() );
		Mat out = new Mat();
		outSum.convertTo( out, CvType.CV_32F, 1.0 / numGradients );
		// Find the maximum point
		MinMaxLocResult mmr = Core.minMaxLoc( out );
		return unscalePoint( mmr.maxLoc, eye );
	}
}
