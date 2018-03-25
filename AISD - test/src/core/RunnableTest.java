package core;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class RunnableTest implements Runnable {

	TestInfo t;

	public RunnableTest(String testPath, String progPath) {
		t = new TestInfo(testPath, progPath);
	}

	@Override
	public void run() {
		try {
			runTest();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	void runTest() throws IOException, InterruptedException {
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

		long time = System.nanoTime();

		for (int i = 0; i < Main.timeIter; i++) {
			Process proc = pb.start();
			exitCode = proc.waitFor();
		}
		time += ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

		t.getResult(tmp, exitCode, (System.nanoTime() - time) / Main.timeIter);
	}
}
