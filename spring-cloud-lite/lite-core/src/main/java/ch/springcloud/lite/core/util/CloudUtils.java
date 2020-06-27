package ch.springcloud.lite.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.springcloud.lite.core.type.VariantType;

public class CloudUtils {

	static List<Class<?>> intclasses = Arrays.asList(int.class, Integer.class, Short.class, short.class, byte.class,
			Byte.class);
	static List<Class<?>> floatclasses = Arrays.asList(float.class, Float.class, Double.class, double.class);

	public static VariantType mapType(Class<?> cls) {
		if (intclasses.contains(cls)) {
			return VariantType.intnum;
		}
		if (floatclasses.contains(cls)) {
			return VariantType.floatnum;
		}
		if (cls == String.class) {
			return VariantType.string;
		}
		if (Map.class.isAssignableFrom(cls)) {
			return VariantType.hash;
		}
		if (List.class.isAssignableFrom(cls) || cls.isArray()) {
			return VariantType.list;
		}
		if (Set.class.isAssignableFrom(cls)) {
			return VariantType.set;
		}
		if (cls.getClassLoader() == null) {
			return VariantType.javasystem;
		}
		if (cls == Void.TYPE) {
			return VariantType.empty;
		}
		return VariantType.hash;
	}
	
	public static String resolveServiceName(String name) {
		StringBuilder realname = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '-' && i < name.length() - 1) {
				i++;
				realname.append(Character.toUpperCase(name.charAt(i)));
			} else {
				realname.append(c);
			}
		}
		return realname.toString();
	}

}
