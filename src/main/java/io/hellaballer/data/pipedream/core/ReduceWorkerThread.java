package io.hellaballer.data.pipedream.core;

import java.util.List;
import java.util.function.BiFunction;

public class ReduceWorkerThread<A> implements Runnable {

	private final BiFunction<A, A, A> funct;
	private final A input1;
	private final A input2;
	private final List<A> output;
	private final int index;
	private final Reducer<A> t;

	public ReduceWorkerThread(BiFunction<A, A, A> funct, A input1, A input2, List<A> outputs, int index, Reducer<A> t) {
		this.funct = funct;
		this.input1 = input1;
		this.input2 = input2;
		this.output = outputs;
		this.index = index;
		this.t = t;

	}

	@Override
	public void run() {
		output.set(index, funct.apply(this.input1, this.input2));
		t.incrementCountState();
	}

}
