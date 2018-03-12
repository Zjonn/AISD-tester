package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CallableTest implements Callable<Integer> {

	Test t;

	public CallableTest(String testPath, String progPath) {
		t = new Test(testPath, progPath);
	}

	@Override
	public Integer call() throws Exception {
		File f = File.createTempFile(t.testPath.getFileName().toString(), "tmp");
		ProcessBuilder pb = new ProcessBuilder(t.progPath.toString());

		pb.redirectInput(new File(t.testPath.toString()));
		pb.redirectOutput(f);
		pb.redirectErrorStream(true);

		Process proc = pb.start();
		int exitCode = proc.waitFor();

		t.updateTest(f, exitCode);
		f.deleteOnExit();

		if (t.isCorrect()) {
			PrintTest.printCorrect(t);
			return 0;
		} else {
			PrintTest.printIncorrect(t);
			return 1;
		}
	}

}

class Test {
	public Path progPath;
	public Path testPath;
	public Path answerPath;

	public int exitCode;
	String answer;
	String output;

	public Test(String testPath, String progPath) {
		this.testPath = Paths.get(testPath);

		testPath = testPath.replace(".in", ".out");
		answerPath = Paths.get(testPath);

		this.progPath = Paths.get(progPath);
	}

	public String getAnswer() {
		if (answer == null)
			try {
				answer = Files.lines(answerPath).collect(Collectors.joining(" "));
			} catch (IOException e) {
				System.err.print("Brak odpowiedzi dla testu " + getTestName());
			}
		return answer;
	}

	public String getOutput(File f) throws IOException {
		if (output == null)
			output = Files.lines(Paths.get(f.getAbsolutePath())).collect(Collectors.joining(" "));
		return output;
	}

	public String getOutput() {
		return output;
	}

	public String getTestName() {
		return testPath.subpath(testPath.getNameCount() - 2, testPath.getNameCount()).toString();
	}

	public void updateTest(File f, int exitCode) throws IOException {
		getAnswer();
		getOutput(f);
		this.exitCode = exitCode;
	}

	public boolean isCorrect() {

		return output.trim().equalsIgnoreCase(answer.trim());
	}
}

class PrintTest {
	static boolean printOk = false;
	static boolean moreInfo = false;

	static void printCorrect(Test t) {
		if (printOk)
			System.out.println("OK: \"" + t.getTestName() + "\"");
	}

	static void printIncorrect(Test t) throws IOException {
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

	private static void printWA(Test t) {
		System.out.println("WA: (exit code: " + t.exitCode + ") \"" + t.getTestName() + "\"");
		if (moreInfo) {
			System.out.println("  Program zwrócił: \n  " + t.getOutput());
			System.out.println("  Poprawna odpowiedź: \n  " + t.getAnswer());
		}
	}

	private static void printSeg(Test t) {
		System.out.println("RE: (Naruszenie ochrony pamięci) \"" + t.getTestName() + "\"");
	}

	private static void printRE(Test t) {
		System.out.println("RE: (exit code: " + t.exitCode + ") \"" + t.getTestName() + "\"");
	}
}
