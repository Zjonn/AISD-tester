package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestInfo {
	public Path progPath;
	public Path testPath;
	public Path answerPath;

	TestResult tr;

	public TestInfo(String testPath, String progPath) {
		this.testPath = Paths.get(testPath);

		testPath = testPath.replace(".in", ".out");
		answerPath = Paths.get(testPath);

		this.progPath = Paths.get(progPath);

		tr = new TestResult(getTestName());
	}

	public TestResult getResult(File progOut, int exitCode, long time) throws IOException {
		tr.setResult(getAnswer(), getOutput(progOut), time, exitCode);
		return tr;
	}

	private String getAnswer() {
		if (tr.answer == null)
			try {
				tr.answer = Files.lines(answerPath).collect(Collectors.joining(" "));
			} catch (IOException e) {
				System.err.print("Brak odpowiedzi dla testu " + getTestName());
			}
		return tr.answer;
	}

	private String getOutput(File f) throws IOException {
		if (tr.output == null)
			tr.output = Files.lines(Paths.get(f.getAbsolutePath())).collect(Collectors.joining(" "));
		return tr.output;
	}

	private String getTestName() {
		return testPath.subpath(testPath.getNameCount() - 2, testPath.getNameCount()).toString();
	}
}

class TestResult {
	String name;
	String answer;
	String output;

	double time;

	int exitCode;

	boolean isUpdated = false;
	boolean isTLE = false;

	public TestResult(String name) {
		this.name = name;
	}

	public void setResult(String answer, String output, long time, int exitCode) {
		this.answer = answer;
		this.output = output;
		this.time = TimeUnit.NANOSECONDS.toMillis(time) / 1000.0;
		this.exitCode = exitCode;
		isUpdated = true;
	}

	public boolean isCorrect() {
		if (isUpdated && !isTLE)
			return output.trim().equalsIgnoreCase(answer.trim());
		return false;
	}
}
