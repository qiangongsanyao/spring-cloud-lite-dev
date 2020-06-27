package ch.springcloud.lite.core.model;

import ch.springcloud.lite.core.type.VariantType;
import lombok.Data;

@Data
public class CloudInvocation {

	String service;
	String method;
	VariantType[] types;

}
