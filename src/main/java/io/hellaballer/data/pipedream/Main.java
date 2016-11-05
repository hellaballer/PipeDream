package io.hellaballer.data.pipedream;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.io.File;

import io.hellaballer.data.pipedream.core.Mapper;
import io.hellaballer.data.pipedream.core.Reducer;
import io.hellaballer.data.pipedream.core.Sharder;
import io.hellaballer.data.pipedream.ffmpeg.FFMPegWrapper;
import io.hellaballer.data.pipedream.speech.BluMixSpeechRunner;
import io.hellaballer.data.pipedream.speech.Time;

public class Main {

	ExecutorService executor = Executors.newFixedThreadPool(5);
	
	static Function <String, List<File>> fileShard = e -> {
		File[] files =  new File(e).listFiles();
		ArrayList<File> fileList = new ArrayList<File>();
		for (File f: files){
			if(f.toString().endsWith(".mp4")){
				fileList.add(f);
			}
		}
		return fileList;	
	};
	
	
	
	public static void main(String[] args) {
		System.out.println("Starting...");

		Map<String, List<Time>> timeMap = BluMixSpeechRunner.getTimings(
				new File("/home/kyle/code/java/eclipse-gifmaker-experimental/SampleGifMaker/splice/outputLonger.wav"));
		System.out.println(timeMap);

		double input = 500;

		System.out.println("INPUT: " + input);

		Sharder<Double, Double> sharder = new Sharder<>(input);

		sharder.runShard(e -> Arrays.asList(e / 3, e / 3, e / 3));

		List<Double> out = sharder.getOutputs();
		
		sharder.destroy();

		int numThreads = out.size();
		Mapper<Double, Double> m = new Mapper<>(numThreads);

		m.setInputs(out);

		m.runMap(e -> e * 2);

		Reducer<Double> r = new Reducer<>(numThreads);

		r.setInputs(m.getOutputs());

		m.destroy();

		r.runReduce((a, b) -> a + b);

		System.out.println("FINAL VAL: " + r.getOutput());
		r.destory();
		
		Sharder<String, File> s = new Sharder<>("/Users/itamarlevy-or/fileShardTest");
		s.runShard(fileShard);
		List<File> fileOut = s.getOutputs();
		for(File f: fileOut)
			System.out.println(f);

		// FFMPegWrapper.convertVideosToAudio(new
		// File("/home/kyle/Documents/ObamaData/A_Bold_New_Course_for_NASA.mp4"));
	}
}
