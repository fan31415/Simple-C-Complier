package bit.minisys.minicc.scanner;

public class KLog {
	private static final int debug = 3;
	private static final int cur = 4;
	public static void d(char c) {
		if (cur <= debug) {
			System.out.println(c);
		}
	}
	public static void d(char[] c) {
		if (cur <= debug) {
			System.out.println(c);
		}
	}
	public static void d(String c) {
		if (cur <= debug) {
			System.out.println(c);
		}
	}
	public static void d(int c) {
		if (cur <= debug) {
			System.out.println(c);
		}
	}
	
}
