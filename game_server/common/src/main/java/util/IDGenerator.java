package util;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

	private AtomicInteger nextSequenceId = new AtomicInteger(1);

	public int getNextId() {
		return nextSequenceId.getAndIncrement();
	}
}
