package io.hellaballer.data.pipedream.ffmpeg;

import java.io.File;
import java.io.IOException;

public class FFMPegWrapper {
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
}
