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

import ch.springcloud.lite.core.anno.EnableCloudServer;
import ch.springcloud.lite.core.configuration.CloudServerAutoConfiguration;
import ch.springcloud.lite.core.configuration.CloudServerBaseConguration;
import ch.springcloud.lite.core.properties.CloudServerProperties;

public class CloudServerConfigurationSelector implements ImportSelector, EnvironmentAware {

	ConfigurableWebEnvironment environment;

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		List<String> imports = new ArrayList<>();
		imports.add(CloudServerProperties.class.getName());
		imports.add(CloudServerBaseConguration.class.getName());
		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableCloudServer.class.getName());
		String exposeMethod = (String) attributes.get("exposeMethod");
		if (EnableCloudServer.SPRINGMVC.equals(exposeMethod)) {
			PropertySource<?> propertySource = new PropertySource<String>("cloud-lite-server-properties") {
				public Object getProperty(String name) {
					name = resolveServiceName(name);
					if (name.startsWith(CloudServerProperties.PROPERTIESPREFIX)) {
						if (name.equals(CloudServerProperties.PROPERTIESPREFIX)) {
							return attributes;
						}
						name = name.replaceAll("^" + CloudServerProperties.PROPERTIESPREFIX + "\\.", "");
						return attributes.get(name);
					} else {
						return null;
					}
				}
			};
			environment.getPropertySources().addLast(propertySource);
			imports.add(CloudServerAutoConfiguration.class.getName());
		} else {
			throw new IllegalArgumentException("Unknown expose method " + exposeMethod + "!");
		}
		return imports.toArray(new String[imports.size()]);
	}

	protected String resolveServiceName(String name) {
		StringBuilder realname = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '-' && i < name.length() - 1) {
				i++;
				realname.append(Character.toUpperCase(name.charAt(i)));
			} else {
				realname.append(c);
			}
		}
		return realname.toString();
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = (ConfigurableWebEnvironment) environment;
	}

}
