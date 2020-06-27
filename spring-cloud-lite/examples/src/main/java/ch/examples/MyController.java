package ch.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

	@Autowired
	MyService myService;

	@GetMapping("/call")
	public Object call() {
		Object object= myService.call();
		System.out.println(object);
		System.out.println(object.getClass());
		return object;
	}

}
