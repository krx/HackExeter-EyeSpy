package hack.exeter.eyespy;

/**
 * @author Kareem El-Faramawi
 */
public final class Constants {
	
	private Constants() {}
	
	// Used for Debugging
	public static final boolean kPlotVectorField = false;
	
	// Size Constants
	public static final int EYE_PERCENT_TOP = 25;
	public static final int EYE_PERCENT_SIDE = 13;
	public static final int EYE_PERCENT_HEIGHT = 30;
	public static final int EYE_PERCENT_WIDTH = 35;
	
	// Algorithm Parameters
	public static final int FAST_EYE_WIDTH = 35;
	public static final int WEIGHT_BLUR_SIDES = 5;
	public static final boolean ENABLE_WEIGHT = true;
	public static final float WEIGHT_DIVISOR = 1.0f;
	public static final double GRADIENT_THRESHOLD = 50.0;
}
