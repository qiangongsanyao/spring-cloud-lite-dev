package ch.springcloud.lite.core.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.context.ConfigurableWebEnvironment;

import ch.springcloud.lite.core.anno.EnableCloudClient;
import ch.springcloud.lite.core.anno.EnableCloudServer;
import ch.springcloud.lite.core.configuration.CloudClientAutoConfiguration;
import ch.springcloud.lite.core.interceptor.SclMethodIntereptor;
import ch.springcloud.lite.core.processor.RemoteFieldPostProcessor;
import ch.springcloud.lite.core.properties.CloudClientProperties;
import ch.springcloud.lite.core.task.ClouClientInitTasks;
import ch.springcloud.lite.core.util.CloudUtils;

public class CloudClientConfigurationSelector implements ImportSelector, EnvironmentAware {

	ConfigurableWebEnvironment environment;

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		List<String> imports = new ArrayList<>();
		imports.add(RemoteFieldPostProcessor.class.getName());
		imports.add(SclMethodIntereptor.class.getName());
		imports.add(CloudClientProperties.class.getName());
		imports.add(ClouClientInitTasks.class.getName());
		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableCloudClient.class.getName());
		String exposeMethod = (String) attributes.get("exposeMethod");
		if (EnableCloudServer.SPRINGMVC.equals(exposeMethod)) {
			PropertySource<?> propertySource = new PropertySource<String>("cloud-lite-client-properties") {
				public Object getProperty(String name) {
					name = CloudUtils.resolveServiceName(name);
					if (name.startsWith(CloudClientProperties.PROPERTIESPREFIX)) {
						if (name.equals(CloudClientProperties.PROPERTIESPREFIX)) {
							return attributes;
						}
						name = name.replaceAll("^" + CloudClientProperties.PROPERTIESPREFIX + "\\.", "");
						return attributes.get(name);
					} else {
						return null;
					}
				}
			};
			environment.getPropertySources().addLast(propertySource);
			imports.add(CloudClientAutoConfiguration.class.getName());
		} else {
			throw new IllegalArgumentException("Unknown expose method " + exposeMethod + "!");
		}
		return imports.toArray(new String[imports.size()]);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = (ConfigurableWebEnvironment) environment;
	}
}
