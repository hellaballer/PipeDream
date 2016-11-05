package io.hellaballer.data.pipedream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	public static void storeTimes(String pathName, Map<String, List<Time>> timeMap) {
		Path path = Paths.get(pathName);
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (Map.Entry<String, List<Time>> entry : timeMap.entrySet()) {
				writer.write(entry.getKey() + ":\n");
				for (Time t : entry.getValue()) {
					writer.write("\t(" + t.getStart() + ", " + t.getEnd() + ") " + t.getVideo() + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, List<Time>> retrieveTimes(String pathName) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(pathName));
			Map<String, List<Time>> timeMap = new HashMap<String, List<Time>>();
			String word = null;
			List<Time> times = null;

			for (String str : lines) {
				if (str.endsWith(":")) {
					if (word != null) {
						timeMap.put(word, times);
					}
					word = str.substring(0, str.length());
					times = new ArrayList<Time>();
				} else {
					double start = Double.parseDouble(str.substring(str.indexOf('(') + 1, str.indexOf(',')));
					double end = Double.parseDouble(str.substring(str.indexOf(',') + 2, str.indexOf(')')));
					String path = str.substring(str.indexOf(')') + 2);
					times.add(new Time(new File(path), start, end));
				}
			}
			timeMap.put(word, times);
			return timeMap;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

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
		args = new String[] { "/home/kyle/Documents/ObamaData/test/",
				"/home/kyle/Documents/ObamaData/output/output.txt" };

		if (args.length < 2) {
			System.out.println("Need input folder and output folder");
			System.exit(-1);
		}

		System.out.println("Starting...");

		String videoDir = args[0];
		String outputFile = args[1];
		// args = new String[] { "/home/kyle/Documents/ObamaData/test/",
		// "/home/kyle/Documents/ObamaData/output/" };
		//
		// if (args.length < 2) {
		// System.out.println("Need input folder and output folder");
		// System.exit(-1);
		// }
		//
		// System.out.println("Starting...");
		//
		// String videoDir = args[0];

		// FFMPegWrapper.concatVideos(
		// Arrays.asList(new
		// File("/home/kyle/Documents/ObamaData/test/A_Bold_New_Course_for_NASA.mp4"),
		// new File("/home/kyle/Documents/ObamaData/test/outputLonger.mp4")),
		// "/home/kyle/Documents/ObamaData/test/out.mp4");
		//
		// System.out.println("concat");

		// FFMPegWrapper.cutVideo(new Time(new File(videoDir +
		// "outputLonger.mp4"), 0, 2), "outputPath.mp4");
		// System.out.println("Done");

		Sharder<String, File> shard = new Sharder<>(videoDir);

		shard.runShard(fileShard);

		List<File> files = shard.getOutputs();
		System.out.println(files);
		shard.destroy();

		Mapper<File, Map<String, List<Time>>> map = new Mapper<>(files.size());
		map.setInputs(files);
		map.runMap(wordMap);
		List<Map<String, List<Time>>> mapedFiles = map.getOutputs();
		map.destroy();

		System.out.println("got mapped files");

		Reducer<Map<String, List<Time>>> reducer = new Reducer<>(files.size());
		reducer.setInputs(mapedFiles);
		reducer.runReduce(reduce);
		Map<String, List<Time>> output = reducer.getOutput();

		storeTimes(outputFile, output);

		// FFMPegWrapper.cutVideo(new Time(new File(videoDir +
		// "outputLonger.mp4"), 0, 2), "outputPath.mp4");
		// System.out.println("Done");
		//
		// Sharder<String, File> shard = new Sharder<>(videoDir);
		//
		// shard.runShard(fileShard);
		//
		// List<File> files = shard.getOutputs();
		//
		// shard.destroy();
		//
		// Mapper<File, Map<String, List<Time>>> map = new
		// Mapper<>(files.size());
		// map.setInputs(files);
		// map.runMap(wordMap);
		// List<Map<String, List<Time>>> mapedFiles = map.getOutputs();
		// map.destroy();
		//
		// Reducer<Map<String, List<Time>>> reducer = new
		// Reducer<>(files.size());
		// reducer.setInputs(mapedFiles);
		// reducer.runReduce(reduce);
		// Map<String, List<Time>> output = reducer.getOutput();

		// System.out.println("FINAL VAL: " + r.getOutput());
		// r.destory();

		// Sharder<String, File> s = new
		// Sharder<>("/Users/itamarlevy-or/fileShardTest");
		// s.runShard(fileShard);
		// List<File> fileOut = s.getOutputs();
		// for(File f: fileOut)
		// System.out.println(f);

		Map<String, List<Time>> mapTest = new HashMap<String, List<Time>>();
		List<Time> times1 = new ArrayList<Time>();
		times1.add(new Time(new File("win"), 1.1, 1.11));
		List<Time> times2 = new ArrayList<Time>();
		times2.add(new Time(new File("another"), 2.1, 2.11));
		times2.add(new Time(new File("one"), 2.2, 2.21));
		List<Time> times3 = new ArrayList<Time>();
		times3.add(new Time(new File("D"), 3.1, 3.11));
		times3.add(new Time(new File("J"), 3.2, 3.21));
		times3.add(new Time(new File("Khaled"), 3.3, 3.31));
		List<Time> times4 = new ArrayList<Time>();
		times4.add(new Time(new File("we"), 4.1, 4.11));
		times4.add(new Time(new File("da"), 4.2, 4.21));
		times4.add(new Time(new File("best"), 4.3, 4.31));
		times4.add(new Time(new File("music"), 4.4, 4.41));
		mapTest.put("hella", times1);
		mapTest.put("baller", times2);
		mapTest.put("dot", times3);
		mapTest.put("io", times4);

		storeTimes("/Users/itamarlevy-or/mapTest", mapTest);

		Map<String, List<Time>> testMap = retrieveTimes("/Users/itamarlevy-or/mapTest");

		storeTimes("/Users/itamarlevy-or/mapTest2", testMap);

		// FFMPegWrapper.convertVideosToAudio(new
		// File("/home/kyle/Documents/ObamaData/A_Bold_New_Course_for_NASA.mp4"));

		// System.out.println(output);

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
