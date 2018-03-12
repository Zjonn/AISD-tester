package core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {

	public String progPath = "prac";
	public String testsPath = "tests";

	public Main(String a[]) {
		init(a);
		List<CallableTest> tests = getTests();
		invokeTests(tests);
	}

	void init(String a[]) {
		handleArg(a);
		readConfig();
	}

	void handleArg(String a[]) {
		for (String s : a) {
			switch (s) {
			case "-m":
				PrintTest.moreInfo = true;
				break;
			case "-c":
				PrintTest.printOk = true;
				break;
			case "-r":
				try {
					Files.delete(Paths.get("zjonn.ini"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void readConfig() {
		Path f = Paths.get("zjonn.ini");
		List<String> str;

		try {
			str = Files.readAllLines(f);
		} catch (IOException e) {
			str = createConfig(f);
		}
		progPath = str.get(0).trim();
		testsPath = str.get(1).trim();
	}

	List<String> createConfig(Path f) {
		List<String> str = new ArrayList<String>();
		Scanner sc = new Scanner(System.in);

		System.out.println("Podaj scieżkę do programu:");
		String path = sc.nextLine();

		while (path.isEmpty() || Files.notExists(Paths.get(path))) {
			System.out.println("Plik nie istnieje, podaj poprawną scieżkę:");
			path = sc.nextLine();
		}
		str.add(path);

		System.out.println("Podaj scieżkę do folderu z testami:");
		path = sc.nextLine();

		while (Files.notExists(Paths.get(path))) {
			System.out.println("Folder nie istnieje, podaj poprawną scieżkę:");
			path = sc.nextLine();
		}
		if (path.isEmpty()) {
			path = testsPath;
			System.out.println("Domyślna ścieżka ustawiona: " + Paths.get(testsPath).toString());
		}
		str.add(path);

		sc.close();
		try {
			Files.write(f, str, Charset.forName("UTF-8"));
		} catch (IOException e1) {
		}
		return str;
	}

	List<CallableTest> getTests() {
		ArrayList<CallableTest> tests = new ArrayList<CallableTest>();

		try (Stream<Path> paths = Files.walk(Paths.get(testsPath))) {
			paths.filter(Files::isRegularFile).filter(x -> x.toString().endsWith(".in"))
					.forEach(x -> tests.add(new CallableTest(x.toString(), progPath)));
		} catch (IOException e) {
			System.out.println("Nie znalazłem foldreru \"" + testsPath + "\"");
		}
		return tests;
	}

	void invokeTests(List<CallableTest> tests) {
		ExecutorService executor = Executors.newFixedThreadPool(5);
		List<Future<Integer>> tasks = null;

		try {
			tasks = executor.invokeAll(tests, 5, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			System.out.println("TLE - wina sprawdzaczki");
		}

		int incorrectTests;
		incorrectTests = countIncorrectTests(tasks);
		printResult(tests.size(), incorrectTests);

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}

	void printResult(int tests, int incorrect) {
		if (tests == 0)
			return;
		if (incorrect == 0) {
			System.out.format("Liczba testów - %d\nWszystkie odpowiedzi poprawne", tests);
		} else {
			System.out.format("Liczba testów - %d\nPoprawne      - %d\nNiepoprawne   - %d", tests, tests - incorrect,
					incorrect);
		}
	}

	int countIncorrectTests(List<Future<Integer>> tasks) {
		int incorrect = 0;
		for (Future<Integer> f : tasks) {
			try {
				incorrect += f.get();
			} catch (InterruptedException e) {
				System.out.println("TLE");
				incorrect++;
			} catch (ExecutionException e) {
				System.out.println("ExecutionException");
				incorrect++;
			}
		}
		return incorrect;
	}

	public static void main(String a[]) {
		new Main(a);
	}
}
