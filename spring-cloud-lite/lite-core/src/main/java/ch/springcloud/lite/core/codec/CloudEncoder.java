package ch.springcloud.lite.core.codec;

import ch.springcloud.lite.core.type.VariantType;

public interface CloudEncoder {

	String encode(Object object, VariantType type);

}
