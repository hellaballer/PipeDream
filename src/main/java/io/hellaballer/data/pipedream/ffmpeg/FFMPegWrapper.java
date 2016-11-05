package io.hellaballer.data.pipedream.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import io.hellaballer.data.pipedream.speech.Time;

public class FFMPegWrapper {
	static DecimalFormat formatter = new DecimalFormat("00.000");

	public static void convertVideosToAudio(File video) {
		Process p;
		try {
			p = Runtime.getRuntime().exec("ffmpeg -i " + video.getAbsolutePath() + " -acodec pcm_s16le -ac 2 "
					+ video.getAbsolutePath().substring(0, video.getAbsolutePath().lastIndexOf(".")) + ".wav");
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void cutVideo(File video, Time time) {
		Process p;
		try {
			p = Runtime.getRuntime().exec("ffmpeg -i " + video.getAbsolutePath() + " -acodec pcm_s16le -ac 2 "
					+ video.getAbsolutePath().substring(0, video.getAbsolutePath().lastIndexOf(".")) + ".wav");
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String convertSecsToTimeString(double timeInput) {
		// Convert number of seconds into hours:mins:seconds string
		int timeSeconds = (int) timeInput;
		int hours = timeSeconds / 3600;
		int mins = (timeSeconds % 3600) / 60;
		double secs = timeSeconds % 60 + timeInput % 1;
		String timeString = String.format("%02d:%02d:%s", hours, mins, formatter.format(secs));
		return timeString;
	}
}
