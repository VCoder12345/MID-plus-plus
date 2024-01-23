package utils;

public class Utils {
	public static void printError(String msg, int line, String file) {
		System.err.print(msg + " in line " + line);
		if(file != null) {
			System.err.println(" (" + file + ")");
		}
	}
}
