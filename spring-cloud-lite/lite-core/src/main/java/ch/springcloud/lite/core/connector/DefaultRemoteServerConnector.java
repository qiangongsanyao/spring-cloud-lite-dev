package ch.springcloud.lite.core.connector;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import ch.springcloud.lite.core.codec.CloudDecoder;
import ch.springcloud.lite.core.model.CloudInvocation;
import ch.springcloud.lite.core.model.CloudMethodParam;
import ch.springcloud.lite.core.model.CloudServer;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.server.ServerController;
import ch.springcloud.lite.core.type.AliveStatus;
import ch.springcloud.lite.core.type.VariantType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DefaultRemoteServerConnector implements RemoteServerConnector {

	@Autowired
	RestTemplate cloudTemplate;
	@Autowired
	ApplicationContext ctx;
	@Autowired
	CloudDecoder decoder;
	Map<String, CloudServer> servers = new ConcurrentHashMap<>(16);
	Map<String, WebClient> clients = new ConcurrentHashMap<>(16);

	@Override
	public List<CloudServer> getServers(CloudInvocation invocation) {
		return servers.values().stream()
				.filter(server -> !server.isInactive() && server.getInvocations().contains(invocation))
				.collect(Collectors.toList());
	}

	@Override
	public Object invoke(CloudServer remoteServer, RemoteRequest request, Class<?> type) throws Exception {
		String remoteUrl = remoteServer.getActiveUrl();
		if (!remoteUrl.startsWith("http")) {
			remoteUrl = "http://" + remoteUrl;
		}
		remoteUrl += ServerController.SERVERCONTROLLER + ServerController.INVOKEPATH;
		HttpHeaders headers = ctx.getBean(HttpHeaders.class);
		HttpEntity<RemoteRequest> requestEntity = new HttpEntity<>(request, headers);
		RemoteResponse response = cloudTemplate.postForObject(new URI(remoteUrl), requestEntity, RemoteResponse.class);
		log.info("response {}", response);
		if (response.getType() == VariantType.empty) {
			return null;
		}
		return decoder.decode(response.getVal(), type);
	}

	@Override
	@Async
	public void load(String remoteUrl, boolean update) {
		if (remoteUrl.matches("^[\\w.\\d]+:(\\d+)$")) {
			String matedataPath = "http://" + remoteUrl + ServerController.SERVERCONTROLLER + ServerController.MATADATA;
			for (int i = 0; i < 3; i++)
				try {
					CloudServerMetaData metadata = cloudTemplate.getForObject(matedataPath, CloudServerMetaData.class);
					CloudServer cloudServer = new CloudServer();
					cloudServer.setMeta(metadata);
					cloudServer.setActiveUrl(remoteUrl);
					cloudServer.setFreshtime(System.currentTimeMillis());
					cloudServer.setInvocations(getInvocations(metadata));
					if (update) {
						servers.put(metadata.getServerid(), cloudServer);
					} else {
						servers.putIfAbsent(metadata.getServerid(), cloudServer);
					}
					keep(cloudServer);
					log.info("load server {}", cloudServer);
					break;
				} catch (Throwable e) {
					log.info("load Remote Url {} Error! for {}", matedataPath, e.getMessage());
				}
		}
	}

	void keep(CloudServer cloudServer) {
		String id = cloudServer.getMeta().getServerid();
		if (!clients.containsKey(id)) {
			WebClient webClient = WebClient.create("http://" + cloudServer.getActiveUrl()
					+ ServerController.SERVERCONTROLLER + ServerController.HEARTBEAT);
			WebClient current = clients.putIfAbsent(id, webClient);
			if (current == null) {
				subscribe(webClient, cloudServer);
			}
		}
	}

	void subscribe(WebClient webClient, CloudServer cloudServer) {
		try {
			Mono<AliveStatus> mono = webClient.post().body(Mono.just(cloudServer.getMeta().getServerid()), String.class)
					.retrieve().bodyToMono(AliveStatus.class);
			mono.subscribe(v -> {
				log.info("Alive Status of {} is {}", cloudServer.getMeta().getServerid(), v);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Set<CloudInvocation> getInvocations(CloudServerMetaData metadata) {
		return metadata.getServices().stream().flatMap(service -> service.getMethods().stream().map(method -> {
			CloudInvocation invocation = new CloudInvocation();
			invocation.setService(service.getName());
			invocation.setMethod(method.getName());
			invocation.setTypes(method.getParams().stream().map(CloudMethodParam::getType).collect(Collectors.toList())
					.toArray(new VariantType[method.getParams().size()]));
			return invocation;
		})).collect(Collectors.toSet());
	}

	@Override
	public List<CloudServerMetaData> healthyServers() {
		return servers.values().stream().filter(server -> !server.isInactive()).map(CloudServer::getMeta)
				.collect(Collectors.toList());
	}

}
