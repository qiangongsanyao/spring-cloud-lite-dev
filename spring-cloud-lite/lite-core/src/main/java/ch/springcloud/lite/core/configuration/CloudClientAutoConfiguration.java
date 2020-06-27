package ch.springcloud.lite.core.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import ch.springcloud.lite.core.connector.DefaultRemoteServerConnector;
import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.server.ClientController;

public class CloudClientAutoConfiguration {

	@Autowired
	HttpServletRequest request;

	@Bean
	RemoteServerConnector connector() {
		return new DefaultRemoteServerConnector();
	}

	@Bean
	ClientController clientController() {
		return new ClientController();
	}

	@Bean
	RestTemplate cloudTemplate() {
		return new RestTemplate();
	}

	@Bean
	@Scope("request")
	HttpHeaders headers() {
		HttpHeaders header = new HttpHeaders();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			List<String> cookieList = new ArrayList<String>();
			for (Cookie cookie : cookies) {
				// 将浏览器cookies放入list中
				// System.out.println("当前cookies为:" + cookie.getDomain() + " " +
				// cookie.getName() + ":" + cookie.getValue());
				cookieList.add(cookie.getName() + "=" + cookie.getValue());
			}
			header.put(HttpHeaders.COOKIE, cookieList);
		}
		return header;
	}

}
