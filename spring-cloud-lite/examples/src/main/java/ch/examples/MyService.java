package ch.examples;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.loadBalance.RoundRobinLoadBalance;
import ch.springcloud.lite.core.properties.CloudServerProperties;

@Service
public class MyService {

	@Remote(name = "newService2", url = "localhost:8123", timeout = 1000, loadBalance = RoundRobinLoadBalance.class)
	NewService newService;

	public Date name(String name, int num, CloudServerProperties properties, Map<String, String> hash, Set<String> set,
			List<String> list) {
		return new Date();
	}

	public Object call() {
		return newService.call(1);
	}

}
