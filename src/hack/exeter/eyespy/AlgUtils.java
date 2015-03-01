package hack.exeter.eyespy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

/**
 * Helper functions for the algorithm
 * @author Kareem El-Faramawi
 */
public final class AlgUtils {
	
	private AlgUtils() {}
	
	public static Mat matrixMagnitude( final Mat matX, final Mat matY ) {
		Mat mags = new Mat( matX.rows(), matX.cols(), CvType.CV_64F );
		Core.magnitude( matX, matY, mags );
		return mags;
	}
	
	public static double dynamicThreshold( Mat mat, double stdDevFactor ) {
		MatOfDouble stdMagnGrad = new MatOfDouble();
		MatOfDouble meanMagnGrad = new MatOfDouble();
		Core.meanStdDev( mat, meanMagnGrad, stdMagnGrad );
		double stdDev = stdMagnGrad.get( 0, 0 )[0] / Math.sqrt( mat.rows() * mat.cols() );
		return stdDevFactor * stdDev + meanMagnGrad.get( 0, 0 )[0];
	}
	
}
