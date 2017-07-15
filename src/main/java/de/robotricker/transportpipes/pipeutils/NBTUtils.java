package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jnbt.*;

public class NBTUtils {

	public static int readIntTag(Tag tag, int defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((IntTag) tag).getValue();
	}

	public static float readFloatTag(Tag tag, float defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((FloatTag) tag).getValue();
	}

	public static double readDoubleTag(Tag tag, double defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((DoubleTag) tag).getValue();
	}

	public static long readLongTag(Tag tag, long defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((LongTag) tag).getValue();
	}

	public static String readStringTag(Tag tag, String defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((StringTag) tag).getValue();
	}

	public static byte readByteTag(Tag tag, byte defaultValue) {
		if (tag == null) {
			return defaultValue;
		}
		return ((ByteTag) tag).getValue();
	}

	public static List<Tag> readListTag(Tag tag) {
		if (tag == null) {
			return new ArrayList<>();
		}
		return ((ListTag) tag).getValue();
	}

	public static void saveIntValue(Map<String, Tag> map, String key, int value) {
		map.put(key, new IntTag(key, value));
	}

	public static void saveFloatValue(Map<String, Tag> map, String key, float value) {
		map.put(key, new FloatTag(key, value));
	}

	public static void saveDoubleValue(Map<String, Tag> map, String key, double value) {
		map.put(key, new DoubleTag(key, value));
	}

	public static void saveLongValue(Map<String, Tag> map, String key, long value) {
		map.put(key, new LongTag(key, value));
	}

	public static void saveStringValue(Map<String, Tag> map, String key, String value) {
		map.put(key, new StringTag(key, value));
	}

	public static void saveByteValue(Map<String, Tag> map, String key, byte value) {
		map.put(key, new ByteTag(key, value));
	}

	public static void saveListValue(Map<String, Tag> map, String key, NBTTagType tagType, List<Tag> value) {
		map.put(key, new ListTag(key, tagType, value));
	}

}
