package ch.springcloud.lite.core.loadBalance;

import java.util.List;

import ch.springcloud.lite.core.model.CloudServer;

public interface LoadBalance {

	CloudServer pickOne(List<CloudServer> servers);

}
