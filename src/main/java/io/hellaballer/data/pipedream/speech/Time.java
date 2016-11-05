package io.hellaballer.data.pipedream.speech;

import java.io.File;

public class Time {
	private final File video;
	private final double start;
	private final double end;

	public Time(File video, double start, double end) {
		this.video = video;
		this.start = start;
		this.end = end;
	}

	public File getVideo() {
		return video;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Time [start=" + start + ", end=" + end + "]";
	}
}
