package ch.springcloud.lite.core.model;

import ch.springcloud.lite.core.type.VariantType;
import lombok.Data;

@Data
public class RemoteResponse {

	VariantType type;
	String val;
	boolean timeout;
	boolean error;
	String errormsg;

}
