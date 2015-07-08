package nachos.threads;

import nachos.machine.*;
import java.util.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
     private int wordTransfer;
     private boolean isWordTransfer;
     private Condition2 speakerCond;
     private Condition2 listenerCond;
     private Condition2 paired;
     private Lock lock;
     
    public Communicator() {
    	this.isWordTransfer = false;
    	this.lock = new Lock();
    	this.speakerCond = new Condition2(lock);
    	this.listenerCond = new Condition2(lock);
    	this.paired = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	lock.acquire();

    	while(isWordTransfer){
    		speakerCond.sleep();
    	}
    	this.isWordTransfer = true; 
    	this.wordTransfer = word;
    	listenerCond.wake();
    	paired.sleep();
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	int transferred;
    	lock.acquire();

	while(!isWordTransfer){
		listenerCond.sleep();
	}
	transferred = this.wordTransfer;
	this.isWordTransfer = false;
	speakerCond.wake();
	paired.wake();

	lock.release();
	return transferred;
    }
}
