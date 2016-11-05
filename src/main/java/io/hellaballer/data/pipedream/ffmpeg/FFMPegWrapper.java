package io.hellaballer.data.pipedream.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import io.hellaballer.data.pipedream.speech.Time;

public class FFMPegWrapper {
	static DecimalFormat formatter = new DecimalFormat("00.000");

	public static File convertVideosToAudio(File video) {
		Process p;
		try {
			String musicOut = video.getAbsolutePath().substring(0, video.getAbsolutePath().lastIndexOf(".")) + ".wav";
			p = Runtime.getRuntime()
					.exec("ffmpeg -i " + video.getAbsolutePath() + " -acodec pcm_s16le -ac 2 " + musicOut);
			p.waitFor();
			return new File(musicOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void cutVideo(Time time, String outputPath) {

		// ffmpeg -i -ss 00:00:09.240 -to 00:00:12.360 -vcodec libx264 -acodec
		// libvo_aacenc output2.mp4

		Process p;
		try {
			p = Runtime.getRuntime().exec("ffmpeg -i " + time.getVideo().getAbsolutePath() + " -ss "
					+ convertSecsToTimeString(time.getStart()) + " -to " + convertSecsToTimeString(time.getEnd())
					+ " -vcodec libx264 -acodec libvo_aacenc " + outputPath);
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
