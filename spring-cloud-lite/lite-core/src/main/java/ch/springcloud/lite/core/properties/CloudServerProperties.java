package ch.springcloud.lite.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(CloudServerProperties.PROPERTIESPREFIX)
@Data
public class CloudServerProperties {

	public final static String PROPERTIESPREFIX = "spring.cloud.lite.server";

	boolean exposeSpringService;

	String host;

	int timeout;

}
