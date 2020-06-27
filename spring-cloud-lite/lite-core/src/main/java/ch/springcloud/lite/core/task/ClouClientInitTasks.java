package ch.springcloud.lite.core.task;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.properties.CloudClientProperties;

public class ClouClientInitTasks {

	@Autowired
	CloudClientProperties properties;
	@Autowired
	RemoteServerConnector connector;

	@PostConstruct
	public void initRemoteUrls() {
		properties.getRemoteUrls().forEach(connector::load);
	}

}
