package io.hellaballer.data.pipedream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	ExecutorService executor = Executors.newFixedThreadPool(5);

	public static void main(String[] args) {
		System.out.println("Starting...");

		Sharder<Integer, Integer> sharder = new Sharder<Integer, Integer>(500);

		sharder.runShard(e -> Arrays.asList(e / 3, e / 3, e / 3));

		List<Integer> out = sharder.getOutputs();
		sharder.destroy();
		
		System.out.println("Output: " + out);

		int numThreads = 5;
		Mapper<Integer, Integer> m = new Mapper<>(numThreads);

		List<Integer> inputs = new ArrayList<>(numThreads);
		for (int i = 0; i < numThreads; i++) {
			inputs.add(i + 1);
		}

		System.out.println("In:  " + inputs);

		m.setInputs(inputs);

		m.runMap(e -> e * 2);

		Reducer<Integer> r = new Reducer<>(numThreads);

		r.setInputs(m.getOutputs());

		m.destroy();

		r.runReduce((a, b) -> a + b);

		System.out.println("FINAL VAL " + r.getOutput());
		r.destory();

//		FFMPegWrapper.convertVideosToAudio(new File("/home/kyle/Documents/ObamaData/A_Bold_New_Course_for_NASA.mp4"));
	}
}
