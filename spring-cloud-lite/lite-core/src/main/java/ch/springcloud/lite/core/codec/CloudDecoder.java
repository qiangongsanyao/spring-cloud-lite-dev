package ch.springcloud.lite.core.codec;

public interface CloudDecoder {

	Object decode(String val, Class<?> type);

}
