package ch.springcloud.lite.core.model;

import java.util.Set;

import lombok.Data;

@Data
public class CloudServer {

	CloudServerMetaData meta;
	Set<CloudInvocation> invocations;
	volatile String activeUrl;
	volatile long freshtime;
	volatile boolean inactive;

}
