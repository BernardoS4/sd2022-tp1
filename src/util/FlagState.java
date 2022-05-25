package util;

public class FlagState {
	
	private FlagState() {}

	private static boolean flag;
	
	
	public static void set(boolean f) {
		flag = f;
	}
	
	public static boolean get() {
		return flag ;
	}
}
