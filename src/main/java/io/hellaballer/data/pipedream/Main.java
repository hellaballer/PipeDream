package io.hellaballer.data.pipedream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	
	public static void storeTimes(String pathName, Map<String, List<Time>> timeMap){
		Path path = Paths.get(pathName);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for(Map.Entry<String, List<Time>> entry: timeMap.entrySet()){
				writer.write(entry.getKey() + ":\n");
				for(Time t: entry.getValue()){
					writer.write("\t(" + t.getStart() + ", " + t.getEnd() + ")\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Starting...");

//		Map<String, List<Time>> timeMap = BluMixSpeechRunner.getTimings(
//				new File("/home/kyle/code/java/eclipse-gifmaker-experimental/SampleGifMaker/splice/outputLonger.wav"));
//		System.out.println(timeMap);
		
		

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
		
//		Sharder<String, File> s = new Sharder<>("/Users/itamarlevy-or/fileShardTest");
//		s.runShard(fileShard);
//		List<File> fileOut = s.getOutputs();
//		for(File f: fileOut)
//			System.out.println(f);

		Map<String, List<Time>> mapTest = new HashMap<String, List<Time>>();
		List<Time> times1 = new ArrayList<Time>();
		times1.add(new Time(1.1, 1.11));
		List<Time> times2 = new ArrayList<Time>();
		times2.add(new Time(2.1, 2.11));
		times2.add(new Time(2.2, 2.21));
		List<Time> times3 = new ArrayList<Time>();
		times3.add(new Time(3.1, 3.11));
		times3.add(new Time(3.2, 3.21));
		times3.add(new Time(3.3, 3.31));
		List<Time> times4 = new ArrayList<Time>();
		times4.add(new Time(4.1, 4.11));
		times4.add(new Time(4.2, 4.21));
		times4.add(new Time(4.3, 4.31));
		times4.add(new Time(4.4, 4.41));
		mapTest.put("hella", times1);
		mapTest.put("baller", times2);
		mapTest.put("dot", times3);
		mapTest.put("io", times4);
		
		storeTimes("/Users/itamarlevy-or/mapTest", mapTest);
		
		// FFMPegWrapper.convertVideosToAudio(new
		// File("/home/kyle/Documents/ObamaData/A_Bold_New_Course_for_NASA.mp4"));
	}
}
