package nachos.threads;

import nachos.machine.*;
import java.util.Random;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

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
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }
    
    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());
        
        return getThreadState(thread).getPriority();
    }
    
    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());
        
        return getThreadState(thread).getEffectivePriority();
    }
    
    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());
        
        Lib.assertTrue(priority >= priorityMinimum &&
                       priority <= priorityMaximum);
        
        getThreadState(thread).setPriority(priority);
    }
    
    public boolean increasePriority() {
        boolean intStatus = Machine.interrupt().disable();
        
        KThread thread = KThread.currentThread();
        
        int priority = getPriority(thread);
        if (priority == priorityMaximum)
            return false;
        
        setPriority(thread, priority+1);
        
        Machine.interrupt().restore(intStatus);
        return true;
    }
    
    public boolean decreasePriority() {
        boolean intStatus = Machine.interrupt().disable();
        
        KThread thread = KThread.currentThread();
        
        int priority = getPriority(thread);
        if (priority == priorityMinimum)
            return false;
        
        setPriority(thread, priority-1);
        
        Machine.interrupt().restore(intStatus);
        return true;
    }
    
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
    

    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new LotteryState(thread);
        
        return (LotteryState) thread.schedulingState;
    }
    
    /*Lottery thread queue.*/
    
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        
        return new lotteryQueue(transferPriority);

    }
    
    protected class lotteryQueue extends PriorityScheduler.PriorityQueue {
        lotteryQueue(boolean transferPriority) {
            
            super(transferPriority);
        
        }
        
      
        protected LotteryState pickNextThread() {
            
            int lotteryTotal = 0, i = 0;
            
            int[] Sum = new int[waitQueue.size()];
            
            if (waitQueue.isEmpty()){
                
                return null;
            }
            
           
            
            for (Iterator<KThread> thread = waitQueue.iterator(); ){
                
                Sum[i++] = lotteryTotal += getThreadState(thread).getEffectivePriority();
            }
            int L = random.nextInt(lotteryTotal);
            
           
            for (Iterator<KThread> thread = waitQueue.iterator();){
                
                if (L < Sum[i++]){

                    return getThreadState(thread);
                }
                
            }
            Lib.assertNotReached();
            
            return null;
            
        }
        
    }
    
    protected class LotteryState extends PriorityScheduler.ThreadState {
        
        public LotteryState(KThread thread) {
       
            super(thread);
        
        }
        
        @Override
        public int getEffectivePriority() {
            return getEffectivePriority(new HashSet<LotteryState>());
        }
        
        private int getEffectivePriority(HashSet<LotteryState> set) {
            //			if (effectivePriority != expiredEffectivePriority)
            //				return effectivePriority;
            
            if (set.contains(this)) {

                return priority;
            }
            
            effectivePriority = priority;
            
            for (PriorityQueue queue : donationQueue)
                if (queue.transferPriority)
                    for (KThread thread : queue.waitQueue) {
                        set.add(this);
                        effectivePriority += getThreadState(thread).getEffectivePriority(set);
                        set.remove(this);
                    }
            
            PriorityQueue queue = (PriorityQueue) thread.waitForJoin;
            if (queue.transferPriority)
                for (KThread thread : queue.waitQueue) {
                    set.add(this);
                    effectivePriority += getThreadState(thread).getEffectivePriority(set);
                    set.remove(this);
                }
            
            return effectivePriority;
        }
    }
    
    protected Random random = new Random(25);
}
