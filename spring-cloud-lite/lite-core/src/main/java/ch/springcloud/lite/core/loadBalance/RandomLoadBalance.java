package ch.springcloud.lite.core.loadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ch.springcloud.lite.core.model.CloudServer;

public class RandomLoadBalance implements LoadBalance {

	private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

	@Override
	public CloudServer pickOne(List<CloudServer> servers) {
		int count = RANDOM.nextInt(servers.size());
		return servers.get(count);
	}

}
