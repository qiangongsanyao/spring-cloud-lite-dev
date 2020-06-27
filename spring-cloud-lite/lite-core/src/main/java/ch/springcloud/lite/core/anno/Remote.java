package ch.springcloud.lite.core.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import ch.springcloud.lite.core.loadBalance.LoadBalance;
import ch.springcloud.lite.core.loadBalance.RandomLoadBalance;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Remote {

	@AliasFor("name")
	String value() default "";

	String name() default "";

	String url() default "";

	int retries() default -1;

	int timeout() default -1;

	Class<? extends LoadBalance> loadBalance() default RandomLoadBalance.class;

}
