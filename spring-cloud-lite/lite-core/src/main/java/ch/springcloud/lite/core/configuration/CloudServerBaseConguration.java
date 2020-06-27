package ch.springcloud.lite.core.configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.EnableCloudClient;
import ch.springcloud.lite.core.model.CloudMethod;
import ch.springcloud.lite.core.model.CloudMethodParam;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.CloudService;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.properties.CloudServerProperties;
import ch.springcloud.lite.core.server.DefaultRemoteRequestHandler;
import ch.springcloud.lite.core.server.RemoteRequestHandler;
import ch.springcloud.lite.core.util.CloudUtils;

@EnableCloudClient
public class CloudServerBaseConguration {

	@Autowired
	ApplicationContext app;
	@Value("${server.port:#{8080}}")
	int port;

	@Bean
	@Scope("prototype")
	RemoteResponse timeoutResponse() {
		RemoteResponse response = new RemoteResponse();
		response.setTimeout(true);
		response.setErrormsg("timeout");
		return response;
	}

	@Bean
	RemoteRequestHandler remoteRequestHandler() {
		return new DefaultRemoteRequestHandler();
	}

	@Bean
	ExecutorService remoteTaskPool() {
		int corePoolSize = Runtime.getRuntime().availableProcessors();
		int maximumPoolSize = corePoolSize * 3;
		long keepAliveTime = 30000L;
		TimeUnit unit = TimeUnit.MICROSECONDS;
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maximumPoolSize * 100);
		AtomicInteger threadid = new AtomicInteger(1);
		ThreadFactory factory = new ThreadFactory() {
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.setName("remoteTaskPool-thread-" + threadid.getAndIncrement());
				return thread;
			}
		};
		RejectedExecutionHandler policy = new AbortPolicy();
		ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				factory, policy);
		return pool;
	}

	@Bean
	CloudServerMetaData metaData(CloudServerProperties properties) {
		CloudServerMetaData metaData = new CloudServerMetaData();
		metaData.setServerid(UUID.randomUUID().toString());
		List<CloudService> services = new Vector<>();
		if (properties.isExposeSpringService()) {
			services.addAll(exposedServices());
		}
		List<String> hosts = localhosts();
		String host = properties.getHost();
		if (host != null && !hosts.contains(host)) {
			hosts.add(0, host);
		}
		metaData.setHosts(hosts);
		metaData.setPort(port);
		metaData.setServices(services);
		metaData.setStartTime(System.currentTimeMillis());
		return metaData;
	}

	private List<String> localhosts() {
		try {
			List<String> localhosts = new ArrayList<>();
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress ia = inetAddresses.nextElement();
					String hostAddress = ia.getHostAddress();
					if (ia.isLoopbackAddress()) {
					} else if (ia.isLinkLocalAddress()) {
					} else {
						localhosts.add(hostAddress);
					}
				}
			}
			return localhosts;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	List<CloudService> exposedServices() {
		return Stream.of(app.getBeanDefinitionNames()).map(name -> {
			Class<?> type = app.getType(name);
			boolean isService = type.getAnnotation(Service.class) != null;
			if (!isService) {
				return null;
			} else {
				CloudService service = new CloudService();
				service.setName(name);
				List<CloudMethod> methods = Stream.of(type.getMethods()).filter(method -> method.getModifiers() == 1
						&& !method.isBridge() && method.getDeclaringClass() != Object.class).map(method -> {
							CloudMethod cloudMethod = new CloudMethod();
							cloudMethod.setName(method.getName());
							List<CloudMethodParam> params = Stream.of(method.getParameters()).map(param -> {
								CloudMethodParam methodParam = new CloudMethodParam();
								methodParam.setName(param.getName());
								methodParam.setType(CloudUtils.mapType(param.getType()));
								return methodParam;
							}).collect(Collectors.toList());
							cloudMethod.setParams(params);
							cloudMethod.setReturnType(CloudUtils.mapType(method.getReturnType()));
							return cloudMethod;
						}).collect(Collectors.toList());
				service.setMethods(methods);
				return service;
			}
		}).filter(service -> service != null).collect(Collectors.toList());
	}

}
