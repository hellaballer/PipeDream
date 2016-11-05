package io.hellaballer.data.pipedream.core;

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
	private A output;

	private CountDownLatch intermediateLatch = new CountDownLatch(1);
	private final CountDownLatch finalLatch = new CountDownLatch(1);
	private int countState;

	public Reducer(int numThreads) {
		this.numThreads = numThreads;
		this.countState = 0;
		this.executor = Executors.newFixedThreadPool(numThreads);
		inputs = new ArrayList<>(numThreads);
		intermediate = new ArrayList<>(numThreads);
		output = null;
	}

	public void destory() {
		executor.shutdown();
	}

	public void setInputs(List<A> inputs) {
		if (inputs.size() != numThreads) {
			throw new IllegalArgumentException("Inputs need to match numThreads");
		}
		this.inputs.addAll(inputs);
	}

	public void runReduce(BiFunction<A, A, A> reduceFunction) {
		while (inputs.size() > 1) {
			intermediate.clear();
			A esgrowElem = null;
			if (inputs.size() % 2 != 0) {
				esgrowElem = inputs.remove(0);
			}

			for (int i = 0; i < inputs.size(); i += 2) {
				intermediate.add(null);
			}

			for (int i = 0; i < inputs.size(); i += 2) {
				A elem1 = inputs.get(i);
				A elem2 = inputs.get(i + 1);
				ReduceWorkerThread<A> reducerThread = new ReduceWorkerThread<A>(reduceFunction, elem1, elem2,
						intermediate, i / 2, this);
				executor.execute(reducerThread);
			}
			try {
				intermediateLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (esgrowElem != null) {
				intermediate.add(esgrowElem);
			}

			intermediateLatch = new CountDownLatch(1);
			countState = 0;
			inputs.clear();
			inputs.addAll(intermediate);
		}
		output = inputs.get(0);
		intermediateLatch.countDown();
		finalLatch.countDown();
	}

	public synchronized void incrementCountState() {
		++countState;
		if (countState >= intermediate.size()) {
			intermediateLatch.countDown();
		}
	}

	public A getOutput() {
		try {
			finalLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return output;
	}
}
