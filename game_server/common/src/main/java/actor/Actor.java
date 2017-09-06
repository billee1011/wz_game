package actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Actor implements IActor {

	private final Logger logger = LoggerFactory.getLogger(Actor.class);

	private final BlockingQueue<ActorTask> taskQueue;

	private Thread t = null;

	private AtomicBoolean running = new AtomicBoolean(false);

	private String name = "";

	private AtomicBoolean stopWhenEmpty = new AtomicBoolean(false);

	private AtomicInteger maxTaskCount = new AtomicInteger(0);

	public Actor(String name) {
		this(name, 80 * 1000);
	}

	public Actor(String name, int capacity) {
		this.name = "Actor-" + name;
		if (capacity <= 0) {
			// 无限容量
			this.taskQueue = new LinkedBlockingDeque<ActorTask>();
		} else {
			// 有限容量
			this.taskQueue = new ArrayBlockingQueue<ActorTask>(capacity);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "|" + getThreadName() + "|" + getThreadId();
	}

	public boolean start() {
		if (running.get() == true) {
			return false;
		}
		running.set(true);
		t = new Thread(new TaskRunner(), name);
		t.start();
		return true;
	}

	public void clear() {
		taskQueue.clear();
	}

	/**
	 * 暴力关
	 */
	public void stop() {
		if (running.get() == false) {
			return;
		}
		running.set(false);
		t.interrupt();
	}

	/**
	 * 自动关
	 */
	public void stopWhenEmpty() {
		if (stopWhenEmpty.get() == true) {
			return;
		}
		stopWhenEmpty.set(true);
	}

	public void waitForStop() {
		while (isRunning()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("", e);
				break;
			}
		}
	}

	public int getQueueSize() {
		return this.taskQueue.size();
	}

	public int getMaxQueueSize() {
		return this.maxTaskCount.get();
	}

	public boolean isRunning() {
		return running.get();
	}

	public boolean isStopping() {
		return stopWhenEmpty.get();
	}

	public boolean put(IRunner runner, long millisec) {
		return put(runner, null, null, millisec);
	}

	public boolean put(IRunner runner) {
		return put(runner, null, null, -1);
	}

	public boolean put(IRunner runner, ICallback callback, IActor target) {
		return put(runner, callback, target, -1);
	}

	public boolean put(IRunner runner, ICallback callback, IActor target, long millisec) {
		if (stopWhenEmpty.get()) {
			logger.error("Actor is stopping: " + this.toString() + ", ignore: " + runner + ", " + callback
					+ ", " + target);
			return false;
		}
		if (!running.get()) {
			String msg = "Actor is not running, invalid put: " + this.getThreadName();
			logger.error("", new IllegalStateException(msg));
			return false;
		}
		// 本actor直接运行
		if (Thread.currentThread() == t) {
			ActorTask task = new ActorTask(runner, callback, target);
			try {
				runTask(task);
			} catch (Throwable e) {
				logger.error("", e);
			}
			return true;
		}
		// 加入队列
		try {
			//
			int size = taskQueue.size();
			if (maxTaskCount.get() < size) {
				this.maxTaskCount.set(size);
			}
			ActorTask task = new ActorTask(runner, callback, target);
			if (millisec == 0) { // 满了直接返回
				return taskQueue.offer(task);
			} else if (millisec > 0) { // 满了等待millisec返回
				return taskQueue.offer(task, millisec, TimeUnit.MILLISECONDS);
			} else { // 一直等待
				// System.out.println("Thread interrupted 3 " + Thread.currentThread().getName() + ", " +
				// (Thread.interrupted() ? "True" : "False"));
				// System.out.println("Thread interrupted 4 " + Thread.currentThread().getName() + ", " +
				// (Thread.interrupted() ? "True" : "False"));
				taskQueue.put(task);
				return true;
			}
		} catch (InterruptedException e) {
			logger.error("", e);
			return false;
		}
	}

	public long getThreadId() {
		return t == null ? 0 : t.getId();
	}

	public String getThreadName() {
		return t == null ? "" : t.getName();
	}

	private class TaskRunner implements Runnable {

		public void run() {
			while (running.get()) {
				try {
					if (stopWhenEmpty.get() && taskQueue.isEmpty()) {
						running.set(false);
						break;
					}
					final ActorTask task = taskQueue.poll(1000L, TimeUnit.MILLISECONDS);
					runTask(task);
				} catch (InterruptedException e) {
					logger.error("", e);
				} catch (Throwable e) {
					logger.error("", e);
				}
			}
		}
	}

	private void runTask(final ActorTask task) {
		if (task == null) {
			return;
		}
		final Object result = (task.runner == null ? null : task.runner.run());
		if (task.callback != null && task.target != null) {
			task.target.put(new IRunner() {

				@Override
				public Object run() {
					task.callback.onResult(result);
					return null;
				}
			});
		} else if (task.callback != null) {
			task.callback.onResult(result);
		}
	}
}
