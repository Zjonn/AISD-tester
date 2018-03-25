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
		
		long start = System.nanoTime();

		Process proc = pb.start();
		int exitCode = proc.waitFor();

		long end = System.nanoTime();

		tmp.deleteOnExit();
		t.getResult(tmp, exitCode, end - start);

	}
	

	private void setProcessBuilder(ProcessBuilder pb, File tmp) {
		pb.redirectInput(new File(t.testPath.toString()));
		pb.redirectOutput(tmp);
		pb.redirectErrorStream(true);
	}

}
