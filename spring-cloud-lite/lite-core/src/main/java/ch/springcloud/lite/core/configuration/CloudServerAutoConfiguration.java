package ch.springcloud.lite.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import ch.springcloud.lite.core.server.ServerController;

public class CloudServerAutoConfiguration {

	@Bean
	@DependsOn("remoteRequestHandler")
	ServerController serverController() {
		return new ServerController();
	}

}
