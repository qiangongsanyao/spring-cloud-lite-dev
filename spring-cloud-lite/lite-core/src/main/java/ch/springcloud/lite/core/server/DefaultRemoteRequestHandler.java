package ch.springcloud.lite.core.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.codec.CloudDecoder;
import ch.springcloud.lite.core.codec.CloudEncoder;
import ch.springcloud.lite.core.model.CloudMethodParam;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.type.VariantType;
import ch.springcloud.lite.core.util.CloudUtils;

public class DefaultRemoteRequestHandler implements RemoteRequestHandler {

	@Autowired
	ExecutorService remoteTaskPool;
	@Autowired
	ApplicationContext ctx;
	@Autowired
	CloudServerMetaData metadata;
	@Autowired
	CloudDecoder decoder;
	@Autowired
	CloudEncoder encoder;

	Map<Class<?>, Map<List<VariantType>, Method>> methods = new ConcurrentHashMap<>();

	@Override
	public void handle(RemoteRequest request, DeferredResult<RemoteResponse> result) {
		Runnable task = () -> {
			try {
				if (result.isSetOrExpired()) {
					return;
				} else {
					result.setResult(exec(request));
				}
			} catch (Throwable e) {
				if (!result.isSetOrExpired()) {
					String errormsg = e.getClass() + ":" + e.getMessage();
					RemoteResponse response = error(errormsg);
					result.setResult(response);
				}
			}
		};
		remoteTaskPool.submit(task);
	}

	private RemoteResponse exec(RemoteRequest request) throws Exception {
		if (!exposed(request)) {
			return error("No such service!");
		}
		Object service = ctx.getBean(request.getService());
		if (service == null) {
			return error("No such service!");
		}
		Class<?> cls = service.getClass();
		Method method = findMethod(cls, request.getMethod(), request.getTypes());
		if (method == null) {
			return error("No such method!");
		}
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] args = new Object[parameterTypes.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = decoder.decode(request.getParams()[i], parameterTypes[i]);
		}
		Object returnVal = method.invoke(service, args);
		RemoteResponse response = new RemoteResponse();
		VariantType rType = CloudUtils.mapType(returnVal.getClass());
		response.setType(rType);
		response.setVal(encoder.encode(returnVal, rType));
		return response;
	}

	private boolean exposed(RemoteRequest request) {
		return metadata.getServices().stream().anyMatch(service -> {
			if (!service.getName().equals(request.getService())) {
				return false;
			}
			return service.getMethods().stream().anyMatch(method -> {
				if (!method.getName().equals(request.getMethod())) {
					return false;
				}
				for (int i = 0; i < method.getParams().size(); i++) {
					CloudMethodParam param = method.getParams().get(i);
					if (param.getType() != request.getTypes()[i]) {
						return false;
					}
				}
				return true;
			});
		});
	}

	private Method findMethod(Class<?> cls, String methodname, VariantType[] types) {
		List<VariantType> vTypes = Arrays.asList(types);
		if (!methods.containsKey(cls)) {
			methods.putIfAbsent(cls, new ConcurrentHashMap<>());
		}
		Map<List<VariantType>, Method> typeMethods = methods.get(cls);
		if (!typeMethods.containsKey(vTypes)) {
			Method[] declaredMethods = cls.getDeclaredMethods();
			FM: for (Method method : declaredMethods) {
				if (method.getName().equals(methodname)) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length == types.length) {
						for (int i = 0; i < parameterTypes.length; i++) {
							if (CloudUtils.mapType(parameterTypes[i]) != types[i]) {
								continue FM;
							}
						}
						typeMethods.putIfAbsent(vTypes, method);
					}
				}
			}
		}
		return typeMethods.get(vTypes);
	}

	private RemoteResponse error(String errormsg) {
		RemoteResponse response = new RemoteResponse();
		response.setError(true);
		response.setErrormsg(errormsg);
		return response;
	}

}
