package ch.springcloud.lite.core.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import ch.springcloud.lite.core.loadBalance.LoadBalance;
import lombok.Data;

@ConfigurationProperties(CloudClientProperties.PROPERTIESPREFIX)
@Data
public class CloudClientProperties {

	public final static String PROPERTIESPREFIX = "spring.cloud.lite.client";

	int retries;

	int timeout;

	List<String> remoteUrls;

	Class<? extends LoadBalance> loadBalance;

}
