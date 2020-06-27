package ch.springcloud.lite.core.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.model.CloudServerMetaData;

@RestController(ClientController.CLIENTCONTROLLER)
@RequestMapping(ClientController.CLIENTCONTROLLER)
public class ClientController {

	public final static String CLIENTCONTROLLER = "/cloud-lite-client";
	public final static String SERVERS = "/servers";
	@Autowired
	RemoteServerConnector connector;

	@GetMapping(SERVERS)
	public List<CloudServerMetaData> servers() {
		return connector.healthyServers();
	}

}
