
package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.PriorityQueue;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.*;


/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    
    //COPIED FROM PRIORITY SCHEDULER
   
    
    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 1;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = Integer.MAX_VALUE;
    
   public static ThreadState schedulingState = null;

  private void acquire(KThread thread) {
       acquiredList.add(thread);
   }

public KThread nextThread() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me
	
		if (this.transferPriority && this.holder != null) //if (holder and transfer priority exists)
		{
	
			this.holder.resource.remove(this); //remove from list
	
		}

		if (waitQueue.isEmpty())
            		{ //if (waitQueue is empty)
	    
	    		return null;//return null
			
            		}
	//incomptype thread state -> kthread
            KThread FT = pickNextThread(); //ThreadState FT = pickNextThread();
            
            if (FT != null) { 
            	
                waitQueue.remove(FT); //remove FT from waitQueue
                
                getThreadState(FT).acquire(this); //FT.acquire();
            }
            
            return FT;
        }
        
        
           protected KThread pickNextThread() {
        
       if(isEmpty())
           return null;

       int pos = 0;
       int curr = 0;
       int lotteryTickets;
        Random R = new Random();
       int num;
       
       lotteryTickets = sum();
       num = R.nextInt(lotteryTickets) + 1;
       
       if(num == 1)
           return getThreadState((KThread) threadList.get(0));

       for(int j = 1; j < threadList.size(); j++) {
           KThread thread = (KThread) threadList.get(j);
           curr += getThreadState(thread).getPriority();
           if(curr > num) {
               pos = j;
               break;
           }
       }
       return getThreadState((KThread) threadList.get(pos));
   

               
        }



    protected int sum() {
        
       if(isEmpty())
       
           return 0;
       
       
       int sum = 0;


       for(int j = 0; j < threadList.size(); j++) {
           
           KThread thread = (KThread) threadList.get(j);
           
           sum += getThreadState(thread).getPriority();
           
           if(sum > priorityMaximum) {
               
               sum = priorityMaximum;
               
           }
           
       }
       
       return sum;
       
   }
   
    public boolean isEmpty() {
        
       return threadList.isEmpty();
       
   }
   
   protected ArrayList threadList = new ArrayList();
   	public boolean transferPriority;
	private ThreadState holder = null;   //holder  
        private LinkedList<KThread> waitQueue = new LinkedList<KThread>(); //wait queue
   	protected KThread thread;
   	   protected ArrayList acquiredList = new ArrayList();



}
   
