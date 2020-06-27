package ch.springcloud.lite.core.loadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ch.springcloud.lite.core.model.CloudServer;

public class RoundRobinLoadBalance implements LoadBalance {

	AtomicInteger count = new AtomicInteger();

	@Override
	public CloudServer pickOne(List<CloudServer> servers) {
		return servers.get(count.getAndIncrement() % servers.size());
	}

}
