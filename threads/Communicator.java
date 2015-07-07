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
     private boolean transfer;
     private Condition2 speakerCond;
     private Condition2 listenerCond;
     private Lock lock;
     private int listener;
     private int speaker;
     private int word;
     
    public Communicator() {
    	this.transfer = false;
    	this.lock = new Lock();
    	this.speakerCond = new Condition2(lock);
    	this.listenerCond = new Condition2(lock);
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
    public void speak() {
    	lock.acquire();
    	speaker++;
    	
    	while(listener==0 || transfer){
    		speakerCond.sleep();
    	}
    	this.word = word; 
    	transfer = true;
    	listenerCond.wakeAll();
    	speaker--;
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	lock.acquire();
	listener++;
	
	while(transfer = false){
		speakerCond.wakeAll();
		listenerCond.sleep();
	}
	
	word = this.word;
	transfer = false;
	listener--;
	lock.release();
	return word;
    }
}
