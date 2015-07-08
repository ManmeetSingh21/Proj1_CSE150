package nachos.threads;

import nachos.machine.*;

import java.util.*;			//cause we need linkedlist

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
	    
	    wakeQueue = new LinkedList<WakeThread>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    	boolean intStatus = Machine.interrupt().disable();
    	long MachineTime = Machine.timer().getTime();
    	for(int i=0; i < wakeQueue.size(); i++){
    		WakeThread wakingThread = wakeQueue.get(i);
    			if (wakingThread.MachineTime <= MachineTime) {
    				wakingThread.wakeThread.ready();		//put the thread back into queue
    				wakeQueue.remove(i--);			//removes the previous
    			}
    	}
	KThread.currentThread().yield();
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
    	boolean intStatus = Machine.interrupt().disable();
	// for now, cheat just to get something working (busy waiting is bad)
	long MachineTime = Machine.timer().getTime() + x;
	
	WakeThread wakingThread = new WakeThread(MachineTime, KThread.currentThread());
	wakeQueue.add(wakingThread);
	KThread.sleep();
	
	Machine.interrupt().restore(intStatus);
    }
    
    private class WakeThread{
    	WakeThread(long wakingThread, KThread wakingCurrentThread){
    		MachineTime = wakingThread;
    		wakeThread = wakingCurrentThread;
    	}
    		public long MachineTime;
    		public KThread wakeThread;
    }
    
    private LinkedList<WakeThread> wakeQueue;
    
    
    
    
}
