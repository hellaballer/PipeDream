package io.hellaballer.data.pipedream;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.hellaballer.data.pipedream.core.Mapper;
import io.hellaballer.data.pipedream.core.Reducer;
import io.hellaballer.data.pipedream.core.Sharder;
import io.hellaballer.data.pipedream.ffmpeg.FFMPegWrapper;
import io.hellaballer.data.pipedream.speech.BluMixSpeechRunner;
import io.hellaballer.data.pipedream.speech.Time;

public class Main {

	static Function<String, List<File>> fileShard = e -> {
		File[] files = new File(e).listFiles();
		List<File> fileList = new ArrayList<File>();
		for (File f : files) {
			if (f.toString().endsWith(".mp4")) {
				fileList.add(f);
			}
		}
		return fileList;
	};

	static Function<File, Map<String, List<Time>>> wordMap = video -> {
		Map<String, List<Time>> map;
		File audio = FFMPegWrapper.convertVideosToAudio(video);
		map = BluMixSpeechRunner.getTimings(video, audio);
		return map;
	};

	static BiFunction<Map<String, List<Time>>, Map<String, List<Time>>, Map<String, List<Time>>> reduce = (map1,
			map2) -> {
		Map<String, List<Time>> newMap = new HashMap<>();
		newMap.putAll(map1);
		newMap.putAll(map2);
		return newMap;
	};

	public static void main(String[] args) {
		args = new String[] { "/home/kyle/Documents/ObamaData/test/" };
		
		if (args.length < 1) {
			System.out.println("Need input folder");
			System.exit(-1);
		}

		System.out.println("Starting...");

		String videoDir = args[0];

		FFMPegWrapper.cutVideo(new Time(new File(videoDir + "outputLonger.mp4"), 0, 2), "outputPath.mp4");
		System.out.println("Done");

		Sharder<String, File> shard = new Sharder<>(videoDir);

		shard.runShard(fileShard);

		List<File> files = shard.getOutputs();

		shard.destroy();

		Mapper<File, Map<String, List<Time>>> map = new Mapper<>(files.size());
		map.setInputs(files);
		map.runMap(wordMap);
		List<Map<String, List<Time>>> mapedFiles = map.getOutputs();
		map.destroy();

		Reducer<Map<String, List<Time>>> reducer = new Reducer<>(files.size());
		reducer.setInputs(mapedFiles);
		reducer.runReduce(reduce);
		Map<String, List<Time>> output = reducer.getOutput();

		System.out.println(output);
	}

	// Map<String, List<Time>> timeMap = BluMixSpeechRunner.getTimings(null,
	// new
	// File("/home/kyle/code/java/eclipse-gifmaker-experimental/SampleGifMaker/splice/outputLonger.wav"));
	// System.out.println(timeMap);
	//
	// double input = 500;
	//
	// System.out.println("INPUT: " + input);
	//
	// Sharder<Double, Double> sharder = new Sharder<>(input);
	//
	// sharder.runShard(e -> Arrays.asList(e / 3, e / 3, e / 3));
	//
	// List<Double> out = sharder.getOutputs();
	//
	// sharder.destroy();
	//
	// int numThreads = out.size();
	// Mapper<Double, Double> m = new Mapper<>(numThreads);
	//
	// m.setInputs(out);
	//
	// m.runMap(e -> e * 2);
	//
	// Reducer<Double> r = new Reducer<>(numThreads);
	//
	// r.setInputs(m.getOutputs());
	//
	// m.destroy();
	//
	// r.runReduce((a, b) -> a + b);
	//
	// System.out.println("FINAL VAL: " + r.getOutput());
	// r.destory();
	//
	// Sharder<String, File> s = new
	// Sharder<>("/Users/itamarlevy-or/fileShardTest");
	// s.runShard(fileShard);
	// List<File> fileOut = s.getOutputs();
	// for (File f : fileOut)
	// System.out.println(f);
	//
	// FFMPegWrapper.convertVideosToAudio(new
	// File("/home/kyle/Documents/ObamaData/A_Bold_New_Course_for_NASA.mp4"));
}
