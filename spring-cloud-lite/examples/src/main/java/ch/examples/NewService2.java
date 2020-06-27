package ch.examples;

import org.springframework.stereotype.Service;

@Service
public class NewService2 implements NewService {

	@Override
	public Result call(int i) {
		Result result = new Result();
		result.setResult(getClass().getName());
		result.setId(i);
		return result;
	}

}
