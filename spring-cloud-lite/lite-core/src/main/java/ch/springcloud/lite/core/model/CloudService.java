package ch.springcloud.lite.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CloudService {

	String name;
	List<CloudMethod> methods;

}
