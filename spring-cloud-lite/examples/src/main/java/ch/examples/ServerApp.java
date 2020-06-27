package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ch.springcloud.lite.core.anno.EnableCloudServer;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableCloudServer
public class ServerApp {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}

}
