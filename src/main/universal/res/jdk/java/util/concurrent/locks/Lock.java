package java.util.concurrent;

public interface Lock {
	
	void lock();
	
	boolean tryLock();
	
	boolean unlock();	
}