package common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by think on 2017/3/29.
 */
public class Threads {
	private ExecutorService[] services;
	private String name;
	private AtomicInteger id = new AtomicInteger(1);

	public Threads(int count, String name) {
		this.services = new ExecutorService[count];
		for (int i = 0; i < count; i++) {
			this.services[i] = Executors.newSingleThreadExecutor();
		}
		this.name = name;
	}

	public void execute(int id, Runnable runnable) {
		ExecutorService service = getExecutors(id);
		service.execute(runnable);
	}

	public ExecutorService getExecutors(int id) {
		return services[Math.abs(id % services.length)];
	}

	class NameThreadFactory implements ThreadFactory {

		public Thread newThread(Runnable r) {
			return new Thread(name + "-" + id.getAndIncrement());
		}
	}

	public static void main(String[] args) {
		Threads pool = new Threads(8, "logic");
		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			pool.execute(i, () -> System.out.println(" hahhahaahah"));
		}
		long time2 = System.currentTimeMillis();
		System.out.println(" cost time " + (time2 - time1));
	}
}
