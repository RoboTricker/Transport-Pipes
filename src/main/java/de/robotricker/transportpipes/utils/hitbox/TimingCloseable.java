package de.robotricker.transportpipes.utils.hitbox;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TimingCloseable implements Closeable {

	public static Map<String, Long> timingsTime = new HashMap<>();
	public static Map<String, Long> timings = new HashMap<>();
	public static Map<String, Long> timingsRecord = new HashMap<>();
	public static Map<String, Long> timingsAmount = new HashMap<>();

	private String name;

	public TimingCloseable(String name) {
		this.name = name;
		timingsTime.put(name, System.nanoTime());
		long a = timingsAmount.containsKey(name) ? timingsAmount.get(name) : 0;
		a++;
		timingsAmount.put(name, a);
	}

	@Override
	public void close() {
		long time = System.nanoTime() - timingsTime.get(name);
		timings.put(name, time);
		if (!timingsRecord.containsKey(name)) {
			timingsRecord.put(name, time);
		}
		if (timingsRecord.get(name) < time) {
			timingsRecord.put(name, time);
		}
	}

}
