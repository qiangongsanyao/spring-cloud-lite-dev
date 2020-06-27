package ch.springcloud.lite.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CloudServerMetaData {

	String serverid;
	List<String> hosts;
	int port;
	List<CloudService> services;
	long startTime;

}
