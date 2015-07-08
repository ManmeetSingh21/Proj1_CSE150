package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;		//MAKING A NEW LINKEDLIST! must add this

/*join() {
Disable interrupts;
if (joinQueue not be initiated) {
create a new thread queue (joinQueue) with transfer priority flag opened
joinQueue acquires this thread as holder
}
If (CurrentThread != self) and (status is not Finished) {
add current thread to join queue
sleep current thread
}

Re-enable interrupts;
}
*/
/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = new LinkedList<KThread>();		//a waitQueue to add while it is locked and cannot do the action
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();
	
	waitQueue.add(KThread.currentThread());
	conditionLock.release();
	
	KThread.currentThread().sleep();		//KThread.currentThread().sleep();
	
	conditionLock.acquire();
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();
	
	if(!waitQueue.isEmpty()){
		(waitQueue.removeFirst()).ready();
	}
	
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();
	
	while(!waitQueue.isEmpty()){
		wake();
	}
	
	Machine.interrupt().restore(intStatus);
    }

    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;
    
}
