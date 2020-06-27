package ch.springcloud.lite.core.configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.springcloud.lite.core.codec.DefaultCloudCodec;

@EnableAsync
@EnableScheduling
@ConditionalOnMissingBean(CloudConfiguration.class)
public class CloudConfiguration {

	@Bean
	ObjectMapper cloudMapper() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper;
	}

	@Bean
	DefaultCloudCodec codec(ObjectMapper mapper) {
		return new DefaultCloudCodec(mapper);
	}

	@Bean
	AsyncConfigurer asyncConfigurer() {
		return new AsyncConfigurer() {
			public Executor getAsyncExecutor() {
				int corePoolSize = Runtime.getRuntime().availableProcessors();
				int maximumPoolSize = corePoolSize;
				long keepAliveTime = 30000L;
				TimeUnit unit = TimeUnit.MICROSECONDS;
				BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maximumPoolSize * 100);
				AtomicInteger threadid = new AtomicInteger(1);
				ThreadFactory factory = new ThreadFactory() {
					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable);
						thread.setDaemon(true);
						thread.setName("TaskPool-thread-" + threadid.getAndIncrement());
						return thread;
					}
				};
				RejectedExecutionHandler policy = new AbortPolicy();
				ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
						workQueue, factory, policy);
				return pool;
			}
		};
	}

}
