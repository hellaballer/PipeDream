package io.hellaballer.data.pipedream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class Reducer<A> {

	private final int numThreads;
	private final ExecutorService executor;

	private final List<A> inputs;
	private final List<A> intermediate;
	private final A outputs;

	private CountDownLatch latch = new CountDownLatch(1);
	private int numElements;
	private int countState;

	public Reducer(int numThreads) {
		this.numThreads = numThreads;
		this.numElements = numThreads;
		this.countState = 0;
		this.executor = Executors.newFixedThreadPool(numThreads);
		inputs = new ArrayList<>(numThreads);
		intermediate = new ArrayList<>(numThreads);
		outputs = null;
		for (int i = 0; i < numThreads; ++i) {
			inputs.add(null);
			intermediate.add(null);
		}
	}

	public void setInputs(List<A> inputs) {
		int i = 0;
		for (A elem : inputs) {
			this.inputs.set(i, elem);
			++i;
		}
	}

	public void runReduce(BiFunction<A, A, A> reduceFunction) {
		while(numElements > 1) {
			for (int i = 0; i < this.numThreads; ++i) {
				MapWorkerThread<A, B> mapWorkerThread = new MapWorkerThread<>(mapFunction, inputs.get(i), outputs, i, this);
				executor.execute(mapWorkerThread);
			}
		}
	}
	
	public synchronized void incrementCountState() {
		++countState;
		if (countState >= numElements) {
			latch.countDown();
		}
	}	

	public List<B> getOutputs() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return outputs;
	}

	// public void run(Function<A, B> funct, int numThreads) {
	// if (numThreads > this.numThreads) {
	// throw new IllegalArgumentException("Too many threads requested.");
	// }
	//
	// for (int i = 0; i < this.numThreads; ++i) {
	// WorkerThread<A, B> workerThread = new WorkerThread<>(funct, i);
	// executor.execute(workerThread);
	// }
	// }

}
