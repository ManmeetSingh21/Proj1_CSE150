
package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.PriorityQueue;
import nachos.threads.PriorityScheduler.ThreadState;
import java.util.LinkedList;
import java.util.Iterator;
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
  	
	 	Lib.assertTrue(Machine.interrupt().disabled()); //no interrupts
	

	
       acquiredList.add(thread);
   }
 public void removeQueue(LotteryScheduler lottery) {           
       acquiredList.remove(lottery);                          
   }
   
  public KThread nextThread() {
       boolean initial = Machine.interrupt().disable();
       
       if(holder!= null)
           holder = null;

       if (isEmpty()) {
       	
           holder = null;
           Machine.interrupt().restore(initial);
           return null;
           
       }

       holder = pickNextThread().thread;

       if(holder != null) {
       	
           threadList.remove(holder);
           acquire(holder);
           
       }
       
       Machine.interrupt().restore(initial);
       
       return holder;
   }
        
           protected KTHread pickNextThread() {
        
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
       
       
       int curr = 0;


       for(int j = 0; j < threadList.size(); j++) {
           
           KThread thread = (KThread) threadList.get(j);
           
           curr += getThreadState(thread).getPriority();
           
           if(curr > priorityMaximum) {
               
               curr = priorityMaximum;
               
           }
           
       }
       
       return curr;
       
   }
   
    public boolean isEmpty() {
        
       return threadList.isEmpty();
       
   }
   
   protected ArrayList threadList = new ArrayList();
   	public boolean transferPriority;
	private KThread holder = null;   //holder  
        private LinkedList<KThread> waitQueue = new LinkedList<KThread>(); //wait queue
   	protected KThread thread;
   	   protected ArrayList acquiredList = new ArrayList();





}
   
