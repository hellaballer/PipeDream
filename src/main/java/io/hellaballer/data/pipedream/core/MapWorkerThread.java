package io.hellaballer.data.pipedream.core;

import java.util.List;
import java.util.function.Function;

public class MapWorkerThread<A, B> implements Runnable {

	private final Function<A, B> funct;
	private final A input;
	private final List<B> output;
	private final int index;
	private final Mapper<A, B> t;

	public MapWorkerThread(Function<A, B> funct, A input, List<B> outputs, int index, Mapper<A, B> t) {
		this.funct = funct;
		this.input = input;
		this.output = outputs;
		this.index = index;
		this.t = t;

	}

	@Override
	public void run() {
		output.set(index, funct.apply(this.input));
		t.incrementCountState();
	}

}
