package org.openlids.join;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayQueue<K> extends ArrayBlockingQueue<K> {
	private static final long serialVersionUID = 1L;

	public ArrayQueue(int arg0) {
		super(arg0);
	}
	
	public synchronized K putR(K obj) {
		K removed = null;
		if (remainingCapacity() < 1) {
			removed = poll();
		}
		
		try {
			put(obj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return removed;
	}

}
