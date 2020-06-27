package ch.springcloud.lite.core.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ch.springcloud.lite.core.codec.CloudEncoder;
import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.loadBalance.LoadBalance;
import ch.springcloud.lite.core.model.CloudInvocation;
import ch.springcloud.lite.core.model.CloudServer;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.properties.CloudClientProperties;
import ch.springcloud.lite.core.type.VariantType;
import ch.springcloud.lite.core.util.CloudUtils;
import lombok.extern.slf4j.Slf4j;

@Scope("prototype")
@Slf4j
public class SclMethodIntereptor implements MethodInterceptor {

	Map<String, Object> remoteAttributes;
	@Autowired
	HttpServletRequest request;
	@Autowired
	CloudEncoder encoder;
	@Autowired
	RemoteServerConnector connector;
	@Autowired
	CloudClientProperties properties;

	volatile LoadBalance loadBalance;

	@Override
	public Object intercept(Object bean, Method method, Object[] params, MethodProxy proxy) throws Throwable {
		Assert.isTrue(remoteAttributes != null, "RemoteAttributes must Not Be Null!");
		if (method.getDeclaringClass() == Object.class) {
			return proxy.invoke(this, params);
		}
		RemoteRequest request = wrapper(method, params);
		String remoteUrl = (String) remoteAttributes.get("url");
		CloudServer remoteServer;
		List<CloudServer> alterUrls = new ArrayList<>();
		Assert.isTrue(remoteUrl != null, "RemoteUrl must Not Be Null!");
		if (!StringUtils.isEmpty(remoteUrl)) {
			List<CloudServer> servers = connector.getServers(mapToInvocation(request));
			if (servers.isEmpty()) {
				throw new RuntimeException("No remote server for " + request);
			}
			LoadBalance loadBalalce = getLoadBalalce();
			remoteServer = loadBalalce.pickOne(servers);
			alterUrls.addAll(servers);
			alterUrls.remove(remoteServer);
		} else {
			remoteServer = new CloudServer();
			remoteServer.setActiveUrl(remoteUrl);
		}
		int trycount = 1;
		int retries = (int) remoteAttributes.get("retries");
		if (retries < 0) {
			retries = properties.getRetries();
		}
		if (retries > 0) {
			trycount += retries;
		}
		Class<?> returnType = method.getReturnType();
		long timeout = Long.parseLong(String.valueOf(remoteAttributes.get("timeout")));
		if (timeout <= 0) {
			timeout = properties.getTimeout();
		}
		request.setTimeout(timeout);
		while (trycount-- > 0) {
			try {
				return connector.invoke(remoteServer, request, returnType);
			} catch (Throwable e) {
				log.warn("connect error {}", e);
			}
		}
		if (!alterUrls.isEmpty()) {
			try {
				return connector.invoke(remoteServer, request, returnType);
			} catch (Throwable e) {
				log.warn("connect error {}", e);
			}
		}
		throw new RuntimeException("Invoke error " + request);
	}

	LoadBalance getLoadBalalce() throws InstantiationException, IllegalAccessException {
		if (loadBalance == null) {
			synchronized (this) {
				if (loadBalance == null) {
					@SuppressWarnings("unchecked")
					Class<? extends LoadBalance> type = (Class<? extends LoadBalance>) remoteAttributes
							.get("loadBalance");
					loadBalance = type.newInstance();
				}
			}
		}
		return loadBalance;
	}

	CloudInvocation mapToInvocation(RemoteRequest request) {
		CloudInvocation invocation = new CloudInvocation();
		invocation.setTypes(request.getTypes());
		invocation.setService(request.getService());
		invocation.setMethod(request.getMethod());
		return invocation;
	}

	RemoteRequest wrapper(Method method, Object[] params) {
		RemoteRequest request = new RemoteRequest();
		request.setService(serviceName());
		request.setMethod(method.getName());
		VariantType[] types = new VariantType[params.length];
		String[] paramVals = new String[params.length];
		request.setParams(paramVals);
		request.setTypes(types);
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			Parameter parameter = parameters[i];
			VariantType type = CloudUtils.mapType(parameter.getType());
			types[i] = type;
			String val = encoder.encode(params[i], type);
			paramVals[i] = val;
		}
		return request;
	}

	String serviceName() {
		return (String) remoteAttributes.get("name");
	}

	@Override
	public String toString() {
		return serviceName() + ":cglib:proxy@" + hashCode();
	}
}
