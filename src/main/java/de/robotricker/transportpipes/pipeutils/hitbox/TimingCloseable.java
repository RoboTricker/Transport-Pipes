package de.robotricker.transportpipes.pipeutils.hitbox;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TimingCloseable implements Closeable {

	public static Map<String, Long> timingsTime = new HashMap<>();
	public static Map<String, Long> timings = new HashMap<>();
	public static Map<String, Long> timingsRecord = new HashMap<>();

	private String name;

	public TimingCloseable(String name) {
		this.name = name;
		timingsTime.put(name, System.currentTimeMillis());
	}

	@Override
	public void close() {
		long time = System.currentTimeMillis() - timingsTime.get(name);
		timings.put(name, time);
		if (!timingsRecord.containsKey(name)) {
			timingsRecord.put(name, time);
		}
		if (timingsRecord.get(name) < time) {
			timingsRecord.put(name, time);
		}
	}

}
