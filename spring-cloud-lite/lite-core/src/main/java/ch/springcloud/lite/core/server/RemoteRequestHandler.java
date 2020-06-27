package ch.springcloud.lite.core.server;

import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;

public interface RemoteRequestHandler {

	void handle(RemoteRequest request, DeferredResult<RemoteResponse> result);

}
