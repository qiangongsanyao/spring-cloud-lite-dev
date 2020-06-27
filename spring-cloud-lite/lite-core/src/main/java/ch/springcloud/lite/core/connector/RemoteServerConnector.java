package ch.springcloud.lite.core.connector;

import java.util.List;

import org.springframework.scheduling.annotation.Async;

import ch.springcloud.lite.core.model.CloudInvocation;
import ch.springcloud.lite.core.model.CloudServer;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.RemoteRequest;

public interface RemoteServerConnector {

	Object invoke(CloudServer remoteServer, RemoteRequest request, Class<?> type) throws Exception;

	@Async
	default void load(String remoteUrl) {
		load(remoteUrl, false);
	}

	void load(String remoteUrl, boolean update);

	List<CloudServer> getServers(CloudInvocation invocation);

	List<CloudServerMetaData> healthyServers();

}
