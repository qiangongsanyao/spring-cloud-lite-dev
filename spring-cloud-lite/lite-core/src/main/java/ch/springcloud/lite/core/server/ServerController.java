package ch.springcloud.lite.core.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.properties.CloudServerProperties;
import ch.springcloud.lite.core.type.AliveStatus;

@RestController(ServerController.SERVERCONTROLLER)
@RequestMapping(ServerController.SERVERCONTROLLER)
public class ServerController {

	public final static String SERVERCONTROLLER = "/cloud-lite-server";
	public final static String INVOKEPATH = "/scl-invoke";
	public final static String MATADATA = "/metadata";
	public final static String HEARTBEAT = "/heart-beat";

	@Autowired
	CloudServerMetaData metadata;
	@Autowired
	CloudServerProperties properties;
	@Autowired
	RemoteRequestHandler handler;
	@Autowired
	RemoteResponse timeoutResponse;

	@GetMapping(MATADATA)
	public CloudServerMetaData metadata() {
		return metadata;
	}

	@PostMapping(HEARTBEAT)
	public AliveStatus alive(@RequestBody String sid) {
		if (metadata.getServerid().equals(sid)) {
			return AliveStatus.Healthy;
		} else {
			return AliveStatus.NotMe;
		}
	}

	@PostMapping(INVOKEPATH)
	public DeferredResult<RemoteResponse> invoke(@RequestBody RemoteRequest request) {
		Long timeout = request.getTimeout();
		if (timeout <= 0) {
			timeout = (long) properties.getTimeout();
		}
		DeferredResult<RemoteResponse> result = new DeferredResult<>(timeout, timeoutResponse);
		handler.handle(request, result);
		return result;
	}

}
