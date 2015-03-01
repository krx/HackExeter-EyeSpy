package hack.exeter.eyespy;

/**
 * Some nice math utility functions
 * @author Kareem El-Faramawi
 */
public final class MathUtils {
	
	public static double clamp( double val, double min, double max ) {
		return Math.min( max, Math.max( val, min ) );
	}
	
	public static double clamp_i( int val, int min, int max ) {
		return Math.min( max, Math.max( val, min ) );
	}
	
	public static double max( double... nums ) {
		if ( nums.length == 0 ) {
			return 0;
		}
		double max = nums[0];
		for ( int i = 1; i < nums.length; i++ ) {
			max = Math.max( max, nums[i] );
		}
		return max;
	}
	
	public static int max_i( int... nums ) {
		if ( nums.length == 0 ) {
			return 0;
		}
		int max = nums[0];
		for ( int i = 1; i < nums.length; i++ ) {
			max = Math.max( max, nums[i] );
		}
		return max;
	}
	
	public static double absMax( double... nums ) {
		double[] abs = new double[nums.length];
		for ( int i = 0; i < nums.length; i++ ) {
			abs[i] = Math.abs( nums[i] );
		}
		return max( abs );
	}
	
	public static int absMax_i( int... nums ) {
		int[] abs = new int[nums.length];
		for ( int i = 0; i < nums.length; i++ ) {
			abs[i] = Math.abs( nums[i] );
		}
		return max_i( abs );
	}
	
	public static boolean inRange_ex( double val, double min, double max ) {
		return min < val && val < max;
	}
	
	public static boolean inRange( double val, double min, double max ) {
		return min <= val && val <= max;
	}
	
	public static double floor( double val, int places ) {
		double pow = Math.pow( 10, places );
		return Math.floor( val * pow ) / pow;
	}
	
	public static double round( double val, int places ) {
		double pow = Math.pow( 10, places );
		return Math.round( val * pow ) / pow;
	}
	
	public static double ceil( double val, int places ) {
		double pow = Math.pow( 10, places );
		return Math.ceil( val * pow ) / pow;
	}
}
