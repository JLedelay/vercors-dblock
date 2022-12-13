package java.util.concurrent;
import java.util.concurrent.Lock;

public interface ReadWriteLock {
	
	Lock readLock();
	
	Lock writeLock();
}