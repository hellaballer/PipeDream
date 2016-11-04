package io.hellaballer.data.pipedream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threader {

	private final int numThreads;
	private final ExecutorService executor;

	public Threader(int numThreads) {
		this.numThreads = numThreads;
		this.executor = Executors.newFixedThreadPool(numThreads);
	}

	// TODO add higher order function as parameter.
	public void run() {
		for (int i = 0; i < this.numThreads; ++i) {
			WorkerThread workerThread = new WorkerThread(i);
			executor.execute(workerThread);
		}
	}

}
