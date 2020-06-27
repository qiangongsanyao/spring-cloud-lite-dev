package ch.springcloud.lite.core.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.springcloud.lite.core.type.VariantType;

public class DefaultCloudCodec implements CloudCodec {

	ObjectMapper mapper;

	public DefaultCloudCodec(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String encode(Object object, VariantType type) {
		try {
			if (object == null) {
				return "";
			}
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.valueOf(object));
		}
	}

	@Override
	public Object decode(String val, Class<?> type) {
		try {
			if (val == null) {
				return null;
			}
			return mapper.readValue(val, type);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
