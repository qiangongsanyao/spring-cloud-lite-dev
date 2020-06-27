package ch.springcloud.lite.core.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import ch.springcloud.lite.core.configuration.CloudConfiguration;
import ch.springcloud.lite.core.selector.CloudClientConfigurationSelector;

/**
 * Enable Cloud Lite Client!
 * 
 * @author 恋骑士
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Import({ CloudClientConfigurationSelector.class, CloudConfiguration.class })
public @interface EnableCloudClient {

	String exposeMethod() default EnableCloudServer.SPRINGMVC;

	int retries() default 2;

	int timeout() default -1;

}
