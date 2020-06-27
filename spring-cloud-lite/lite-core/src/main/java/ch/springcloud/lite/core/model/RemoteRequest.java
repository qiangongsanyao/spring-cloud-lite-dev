package ch.springcloud.lite.core.model;

import ch.springcloud.lite.core.type.VariantType;
import lombok.Data;

@Data
public class RemoteRequest {

	String service;
	String method;
	VariantType[] types;
	String[] params;
	long timeout;

}
