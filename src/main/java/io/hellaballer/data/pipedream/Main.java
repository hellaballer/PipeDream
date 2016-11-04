package io.hellaballer.data.pipedream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	ExecutorService executor = Executors.newFixedThreadPool(5);

	public static void main(String[] args) {
		System.out.println("Starting...");
		int numThreads = 2;
		Mapper<Integer, String > m = new Mapper<>(numThreads);

		List<Integer> inputs = new ArrayList<>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			inputs.add(i + 1);
		}

		System.out.println("In:  " + inputs);

		m.setInputs(inputs);

		m.runMap(e -> "foo" + String.valueOf(e) + "foo");

		System.out.println("Out: " + m.getOutputs());

	}
}
