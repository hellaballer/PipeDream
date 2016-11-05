package io.hellaballer.data.pipedream;

import java.util.List;
import java.util.function.Function;

public class ShardWorkerThread<A, B> implements Runnable{

	private final Function<A, List<B>> funct;
	private final A input;
	private List<B> output;
	private final Sharder<A, B> t;
	
	public ShardWorkerThread(Function<A, List<B>> funct, A input, List<B> outputs, Sharder<A, B> t) {
		this.funct = funct;
		this.input = input;
		this.output = outputs;
		this.t = t;
	}
	
	public void run() {
		output = funct.apply(this.input);
		t.incrementCountState();
	}
	
	
	
}
