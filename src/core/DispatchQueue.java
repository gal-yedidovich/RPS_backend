package core;

import java.util.ArrayDeque;
import java.util.Queue;

public class DispatchQueue implements Runnable {
	private Thread thread = new Thread(this);
	private Queue<Runnable> queue = new ArrayDeque<>();
	private boolean isAlive;

	public void add(Runnable action) {
		synchronized (this) {
			if (thread.getState() == Thread.State.NEW) {
				isAlive = true;
				thread.start();
			}
			queue.add(action);
		}
	}

	@Override
	public void run() {
		while (isAlive) {
			if (!queue.isEmpty()) queue.remove().run();
			else {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void kill() {
		isAlive = false;
	}
}
