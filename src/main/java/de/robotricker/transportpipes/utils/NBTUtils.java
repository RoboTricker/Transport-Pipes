package de.robotricker.transportpipes.utils;

import java.util.ArrayList;
import java.util.List;

import com.flowpowered.nbt.*;

public class NBTUtils {

	public static int readIntTag(Tag<?> tag, int defaultValue) {
		if (tag == null) {
			return defaultValue;
		} 
		return ((IntTag) tag).getValue();
	}

	public static float readFloatTag(Tag<?> tag, float defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((FloatTag) tag).getValue();
	}

	public static double readDoubleTag(Tag<?> tag, double defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((DoubleTag) tag).getValue();
	}

	public static long readLongTag(Tag<?> tag, long defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((LongTag) tag).getValue();
	}

	public static String readStringTag(Tag<?> tag, String defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((StringTag) tag).getValue();
	}

	public static byte readByteTag(Tag<?> tag, byte defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((ByteTag) tag).getValue();
	}

	public static List<Tag<?>> readListTag(Tag<?> tag) {
		if (tag == null) {
			return new ArrayList<>();
		}
		return ((ListTag) tag).getValue();
	}

	public static void saveIntValue(CompoundMap map, String key, int value) {
		map.put(key, new IntTag(key, value));
	}

	public static void saveFloatValue(CompoundMap map, String key, float value) {
		map.put(key, new FloatTag(key, value));
	}

	public static void saveDoubleValue(CompoundMap map, String key, double value) {
		map.put(key, new DoubleTag(key, value));
	}

	public static void saveLongValue(CompoundMap map, String key, long value) {
		map.put(key, new LongTag(key, value));
	}

	public static void saveStringValue(CompoundMap map, String key, String value) {
		map.put(key, new StringTag(key, value));
	}

	public static void saveByteValue(CompoundMap map, String key, byte value) {
		map.put(key, new ByteTag(key, value));
	}

	public static void saveListValue(CompoundMap map, String key, Class<?> elementType, List<Tag<?>> value) {
		map.put(key, new ListTag(key, elementType, value));
	}

}
