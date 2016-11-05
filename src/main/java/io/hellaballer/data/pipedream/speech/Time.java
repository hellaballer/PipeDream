package io.hellaballer.data.pipedream.speech;

public class Time {
	private final double start;
	private final double end;

	public Time(double start, double end) {
		this.start = start;
		this.end = end;
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
