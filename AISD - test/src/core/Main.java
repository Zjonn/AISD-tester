package core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class Main {

	public String progPath = "prac";
	public String testsPath = "tests";

	public static boolean timeTest = false;
	public static final int timeIter = 10;

	public Main(String a[]) {
		init(a);
		var tests = getTests();
		var results = invokeTests(tests);
		var testResInfo = getResults(tests);
		printTestsResults(results, testResInfo);
	}

	void init(String a[]) {
		handleArg(a);
		readConfig();
	}

	void handleArg(String a[]) {
		for (String s : a) {
			switch (s) {
			case "-m":
			case "--more":
				PrintTest.moreInfo = true;
				break;
			case "-c":
			case "--correct":
				PrintTest.printOk = true;
				break;
			case "-t":
			case "--time":
				PrintTest.printTime = true;
				timeTest = true;
				break;
			case "-r":
			case "--reset":
				try {
					Files.delete(Paths.get("zjonn.ini"));
				} catch (IOException e) {
				}
				break;
			case "-h":
			case "--help":
				System.out.println("Dostępne argumenty:");
				System.out.println("-m  --more     testy dla których program zawiódł będą wypisywane wraz z"
						+ " wartością zwróconą przez program oraz wartością oczekiwaną");
				System.out.println("-c  --correct  sprawdzaczka zacznie wypisywać wszystkie testy którym"
						+ " poddany został program");
				System.out.println("-r  --reset    pozwala na zmianę ścieżki do programu i testów");
				System.out.println("-t  --time     wypisuje czas wykonywania testów, jest to śrenia z " + timeIter
						+ " wykonań tesów (może chwilę zająć)");
				break;
			default:
				System.out.println("Nie znam: " + s);
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
		path = Paths.get(path).toAbsolutePath().toString();
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
		path = Paths.get(path).toAbsolutePath().toString();
		str.add(path);

		sc.close();
		try {
			Files.write(f, str, Charset.forName("UTF-8"));
		} catch (IOException e1) {
		}
		return str;
	}

	List<RunnableTest> getTests() {
		var tests = new ArrayList<RunnableTest>();

		try (Stream<Path> paths = Files.walk(Paths.get(testsPath))) {
			paths.filter(Files::isRegularFile).filter(x -> x.toString().endsWith(".in"))
					.forEach(x -> tests.add(new RunnableTest(x.toString(), progPath)));
		} catch (IOException e) {
			System.out.println("Nie znalazłem foldreru \"" + testsPath + "\"");
		}
		return tests;
	}

	List<Future<Void>> invokeTests(List<RunnableTest> tests) {
		var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		var tasks = new ArrayList<Future<Void>>();

		for (var c : tests) {
			tasks.add(executor.submit(c, null));
		}

		executor.shutdown();
		return tasks;
	}

	List<TestResult> getResults(List<RunnableTest> tests) {
		var l = new ArrayList<TestResult>();
		for (RunnableTest t : tests) {
			l.add(t.t.tr);
		}
		return l;
	}

	void printTestsResults(List<Future<Void>> future, List<TestResult> tr) {
		int incorrect = future.size();
		for (int i = 0; i < future.size(); i++) {
			Future<Void> f = future.get(i);
			TestResult tRes = tr.get(i);
			try {
				f.get();

				if (tRes.isCorrect())
					incorrect--;
			} catch (InterruptedException | ExecutionException e) {
				tRes.isTLE = true;
			}
			printProgress((double) (i + 1) / future.size());
		}
		Collections.sort(tr, new Comparator<TestResult>() {
			@Override
			public int compare(TestResult a0, TestResult a1) {
				return a0.name.compareToIgnoreCase(a1.name);
			}
		});
		for (TestResult t : tr) {
			PrintTest.printResult(t);
		}
		printSummary(future.size(), incorrect);
	}

	void printProgress(double percent) {
		System.out.print("[");
		for (int i = 1; i <= 80; i++) {
			if (i <= percent * 80)
				System.out.print("#");
			else
				System.out.print(" ");
		}
		if (percent >= 1)
			System.out.println("] Done");
		else
			System.out.format("] %d%% \r", (int) (percent * 100));
	}

	void printSummary(int tests, int incorrect) {
		if (tests == 0)
			return;
		if (incorrect == 0) {
			System.out.format("Liczba testów - %d\nWszystkie odpowiedzi poprawne\n", tests);
		} else {
			System.out.format("Liczba testów - %d\nPoprawne      - %d\nNiepoprawne   - %d\n", tests, tests - incorrect,
					incorrect);
		}
	}

	public static void main(String a[]) {
		new Main(a);
	}
}
