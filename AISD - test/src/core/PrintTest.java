package core;

public class PrintTest {
	static boolean printOk = false;
	static boolean moreInfo = false;
	static boolean printTime = false;

	static void printResult(TestResult t) {
		if (t.isCorrect())
			printCorrect(t);
		else if (!t.isUpdated)
			printRunError(t);
		else if (t.isTLE)
			printTLE(t);
		else
			printIncorrect(t);
	}

	static void printCorrect(TestResult t) {
		if (printTime) {
			System.out.println("OK: time: " + t.time + "sec \"" + t.name + "\"");
		} else if (printOk)
			System.out.println("OK: \"" + t.name + "\"");
	}

	static void printTLE(TestResult t) {
		System.out.println("TLE: \"" + t.name + "\"");
	}

	static void printRunError(TestResult t) {
		System.out.println("Invoke Error: \"" + t.name + "\"");
	}

	static void printIncorrect(TestResult t) {
		switch (t.exitCode) {
		case 0:
			printWA(t);
			break;
		case 139:
			printSeg(t);
			break;
		default:
			printRE(t);
		}
	}

	private static void printWA(TestResult t) {
		System.out.println("WA: (exit code: " + t.exitCode + ") \"" + t.name + "\"");
		if (moreInfo) {
			System.out.println("  Program zwrócił: \n  " + t.output);
			System.out.println("  Poprawna odpowiedź: \n  " + t.answer);
		}
	}

	private static void printSeg(TestResult t) {
		System.out.println("RE: (Naruszenie ochrony pamięci) \"" + t.name + "\"");
	}

	private static void printRE(TestResult t) {
		System.out.println("RE: (exit code: " + t.exitCode + ") \"" + t.name + "\"");
	}
}
