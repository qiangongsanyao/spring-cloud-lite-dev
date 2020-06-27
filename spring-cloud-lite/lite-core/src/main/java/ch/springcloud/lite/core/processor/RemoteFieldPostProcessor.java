package ch.springcloud.lite.core.processor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.interceptor.SclMethodIntereptor;

public class RemoteFieldPostProcessor implements BeanPostProcessor, ApplicationContextAware {

	ApplicationContext ctx;

	Map<Class<?>, Map<String, Object>> remoteBeans = new ConcurrentHashMap<>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> type = bean.getClass();
		findRemoteFields(type).forEach((field, remote) -> {
			Object remoteBean = getRemoteBean(field, remote);
			try {
				field.setAccessible(true);
				field.set(bean, remoteBean);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			}
		});
		return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

	private Object getRemoteBean(Field field, Remote remote) {
		Class<?> type = field.getType();
		if (!remoteBeans.containsKey(type)) {
			remoteBeans.putIfAbsent(type, new ConcurrentHashMap<>());
		}
		Map<String, Object> beans = remoteBeans.get(type);
		String servicename = remote.name();
		if (StringUtils.isEmpty(servicename)) {
			servicename = field.getName();
		}
		Map<String, Object> remoteAttributes = AnnotationUtils.getAnnotationAttributes(remote);
		if (!beans.containsKey(servicename)) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(type);
			SclMethodIntereptor intereptor = ctx.getBean(SclMethodIntereptor.class);
			try {
				Field remoteField = intereptor.getClass().getDeclaredField("remoteAttributes");
				remoteField.setAccessible(true);
				remoteField.set(intereptor, remoteAttributes);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			enhancer.setCallback(intereptor);
			Object remoteBean = enhancer.create();
			beans.putIfAbsent(servicename, remoteBean);
		}
		return beans.get(servicename);
	}

	private Map<Field, Remote> findRemoteFields(Class<?> type) {
		Field[] fields = type.getDeclaredFields();
		Map<Field, Remote> table = new HashMap<>();
		for (Field field : fields) {
			Remote annotation = AnnotationUtils.getAnnotation(field, Remote.class);
			if (annotation != null) {
				table.put(field, annotation);
			}
		}
		return table;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx = applicationContext;
	}

}
