package core;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CallableTest implements Callable<Void> {

	TestInfo t;

	public CallableTest(String testPath, String progPath) {
		t = new TestInfo(testPath, progPath);
	}

	@Override
	public Void call() throws Exception {
		callTest();
		return null;
	}

	void callTest() throws IOException, InterruptedException {
		File tmp = File.createTempFile(t.testPath.getFileName().toString(), "tmp");
		ProcessBuilder pb = new ProcessBuilder(t.progPath.toString());
		setProcessBuilder(pb, tmp);

		if (Main.timeTest)
			timeTestInvoke(tmp, pb);
		else
			oneInvoke(tmp, pb);

		tmp.deleteOnExit();
	}

	private void setProcessBuilder(ProcessBuilder pb, File tmp) {
		pb.redirectInput(new File(t.testPath.toString()));
		pb.redirectOutput(tmp);
		pb.redirectErrorStream(true);
	}

	private void oneInvoke(File tmp, ProcessBuilder pb) throws IOException, InterruptedException {
		int exitCode = 0;
		Process proc = pb.start();
		exitCode = proc.waitFor();
		t.getResult(tmp, exitCode, 0);

	}

	private void timeTestInvoke(File tmp, ProcessBuilder pb) throws IOException, InterruptedException {
		int exitCode = 0;

		long start = System.nanoTime();

		for (int i = 0; i < Main.timeIter; i++) {
			Process proc = pb.start();
			exitCode = proc.waitFor();
		}

		long end = System.nanoTime();
		t.getResult(tmp, exitCode, (end - start) / Main.timeIter);
	}

}
