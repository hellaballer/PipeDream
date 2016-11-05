package io.hellaballer.data.pipedream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Sharder<A, B> {
	
	private final ExecutorService executor =Executors.newFixedThreadPool(1);
	
	private final A input;
	private List<B> outputs;
	
	private final CountDownLatch latch = new CountDownLatch(1);
	
	public Sharder(A input){
		this.input = input;
		this.outputs = new ArrayList<>();
	}
	
	public void runShard(Function<A, List<B>> shardFunc){
		ShardWorkerThread<A, B> shardWorkerThread = new ShardWorkerThread<>(shardFunc, input, outputs, this);
		executor.execute(shardWorkerThread);
	}
	
	public synchronized void incrementCountState() {
		latch.countDown();
	}
	
	public List<B> getOutputs(){
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this.outputs;
	}
}
